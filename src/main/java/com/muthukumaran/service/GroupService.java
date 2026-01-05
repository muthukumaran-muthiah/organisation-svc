package com.muthukumaran.organization.service;

import com.muthukumaran.organization.dto.*;
import com.muthukumaran.organization.exception.*;
import com.muthukumaran.organization.model.Group;
import com.muthukumaran.organization.model.GroupStatus;
import com.muthukumaran.organization.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_MEMBERSHIP_KEY = "group:%s:users";
    private static final String USER_GROUP_KEY = "user:%s:group";
    
    /**
     * Create a new group with validation
     */
    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request) {
        log.info("Creating group with name: {}", request.getName());
        
        // Validate parent exists if provided
        if (request.getParentUuid() != null && !request.getParentUuid().isEmpty()) {
            groupRepository.findById(request.getParentUuid())
                .orElseThrow(() -> new ParentGroupNotFoundException(request.getParentUuid()));
        }
        
        // Build group entity
        Group group = Group.builder()
            .uuid(UUID.randomUUID().toString())
            .parentUuid(request.getParentUuid())
            .name(request.getName())
            .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getName())
            .status(request.getStatus() != null ? request.getStatus() : GroupStatus.ACTIVE)
            .spaceId(request.getSpaceId())
            .location(request.getLocation())
            .language(request.getLanguage())
            .segments(request.getSegments())
            .build();
        
        Group savedGroup = groupRepository.save(group);
        log.info("Group created with UUID: {}", savedGroup.getUuid());
        
        return mapToResponse(savedGroup);
    }
    
    /**
     * Get group with inherited properties
     */
    public GroupResponse getGroupWithInheritance(String uuid) {
        log.info("Fetching group with UUID: {}", uuid);
        
        Group group = groupRepository.findById(uuid)
            .orElseThrow(() -> new GroupNotFoundException(uuid));
        
        // Apply inheritance logic
        Group resolvedGroup = resolveInheritance(group);
        
        return mapToResponse(resolvedGroup);
    }
    
    /**
     * Resolve inherited properties by traversing up the hierarchy
     */
    private Group resolveInheritance(Group group) {
        Group resolved = Group.builder()
            .uuid(group.getUuid())
            .parentUuid(group.getParentUuid())
            .name(group.getName())
            .displayName(group.getDisplayName())
            .status(group.getStatus())
            .spaceId(group.getSpaceId())
            .location(group.getLocation())
            .language(group.getLanguage())
            .segments(group.getSegments())
            .build();
        
        // If any inheritable field is null, look up the parent chain
        if (resolved.getSpaceId() == null || 
            resolved.getLocation() == null || 
            resolved.getLanguage() == null || 
            resolved.getSegments() == null) {
            
            String currentParentUuid = group.getParentUuid();
            Set<String> visited = new HashSet<>();
            visited.add(group.getUuid());
            
            while (currentParentUuid != null && !currentParentUuid.isEmpty()) {
                // Prevent circular references
                if (visited.contains(currentParentUuid)) {
                    log.warn("Circular reference detected in group hierarchy at UUID: {}", currentParentUuid);
                    break;
                }
                visited.add(currentParentUuid);
                
                Optional<Group> parentOpt = groupRepository.findById(currentParentUuid);
                if (parentOpt.isEmpty()) {
                    log.warn("Parent group not found: {}", currentParentUuid);
                    break;
                }
                
                Group parent = parentOpt.get();
                
                // Inherit missing fields
                if (resolved.getSpaceId() == null && parent.getSpaceId() != null) {
                    resolved.setSpaceId(parent.getSpaceId());
                }
                if (resolved.getLocation() == null && parent.getLocation() != null) {
                    resolved.setLocation(parent.getLocation());
                }
                if (resolved.getLanguage() == null && parent.getLanguage() != null) {
                    resolved.setLanguage(parent.getLanguage());
                }
                if (resolved.getSegments() == null && parent.getSegments() != null) {
                    resolved.setSegments(parent.getSegments());
                }
                
                // Check if all fields are resolved
                if (resolved.getSpaceId() != null && 
                    resolved.getLocation() != null && 
                    resolved.getLanguage() != null && 
                    resolved.getSegments() != null) {
                    break;
                }
                
                currentParentUuid = parent.getParentUuid();
            }
        }
        
        return resolved;
    }
    
    /**
     * Update an existing group
     */
    @Transactional
    public GroupResponse updateGroup(String uuid, GroupUpdateRequest request) {
        log.info("Updating group with UUID: {}", uuid);
        
        Group group = groupRepository.findById(uuid)
            .orElseThrow(() -> new GroupNotFoundException(uuid));
        
        // Update fields if provided
        if (request.getName() != null) {
            group.setName(request.getName());
        }
        if (request.getDisplayName() != null) {
            group.setDisplayName(request.getDisplayName());
        }
        if (request.getStatus() != null) {
            group.setStatus(request.getStatus());
        }
        if (request.getSpaceId() != null) {
            group.setSpaceId(request.getSpaceId());
        }
        if (request.getLocation() != null) {
            group.setLocation(request.getLocation());
        }
        if (request.getLanguage() != null) {
            group.setLanguage(request.getLanguage());
        }
        if (request.getSegments() != null) {
            group.setSegments(request.getSegments());
        }
        
        Group updatedGroup = groupRepository.save(group);
        log.info("Group updated successfully: {}", uuid);
        
        return mapToResponse(updatedGroup);
    }
    
    /**
     * Delete a group (only if it has no children)
     */
    @Transactional
    public void deleteGroup(String uuid) {
        log.info("Deleting group with UUID: {}", uuid);
        
        Group group = groupRepository.findById(uuid)
            .orElseThrow(() -> new GroupNotFoundException(uuid));
        
        // Check if group has children
        List<Group> children = groupRepository.findByParentUuid(uuid);
        if (!children.isEmpty()) {
            throw new GroupHasChildrenException(uuid);
        }
        
        // Delete user memberships
        String membershipKey = String.format(USER_MEMBERSHIP_KEY, uuid);
        redisTemplate.delete(membershipKey);
        
        // Delete the group
        groupRepository.delete(group);
        log.info("Group deleted successfully: {}", uuid);
    }
    
    /**
     * Add a user to a group
     */
    @Transactional
    public void addUserToGroup(String groupUuid, String userId) {
        log.info("Adding user {} to group {}", userId, groupUuid);
        
        // Verify group exists
        groupRepository.findById(groupUuid)
            .orElseThrow(() -> new GroupNotFoundException(groupUuid));
        
        String membershipKey = String.format(USER_MEMBERSHIP_KEY, groupUuid);
        String userGroupKey = String.format(USER_GROUP_KEY, userId);
        
        // Add user to group's set
        redisTemplate.opsForSet().add(membershipKey, userId);
        
        // Track user's current group
        redisTemplate.opsForValue().set(userGroupKey, groupUuid);
        
        log.info("User {} added to group {}", userId, groupUuid);
    }
    
    /**
     * Remove a user from a group
     */
    @Transactional
    public void removeUserFromGroup(String groupUuid, String userId) {
        log.info("Removing user {} from group {}", userId, groupUuid);
        
        // Verify group exists
        groupRepository.findById(groupUuid)
            .orElseThrow(() -> new GroupNotFoundException(groupUuid));
        
        String membershipKey = String.format(USER_MEMBERSHIP_KEY, groupUuid);
        String userGroupKey = String.format(USER_GROUP_KEY, userId);
        
        // Check if user is in the group
        Boolean isMember = redisTemplate.opsForSet().isMember(membershipKey, userId);
        if (Boolean.FALSE.equals(isMember)) {
            throw new UserNotFoundException(userId);
        }
        
        // Remove user from group's set
        redisTemplate.opsForSet().remove(membershipKey, userId);
        
        // Remove user's group tracking
        redisTemplate.delete(userGroupKey);
        
        log.info("User {} removed from group {}", userId, groupUuid);
    }
    
    /**
     * Move user from current group to target group (atomic operation)
     */
    @Transactional
    public void moveUser(String userId, String targetGroupUuid) {
        log.info("Moving user {} to group {}", userId, targetGroupUuid);
        
        // Verify target group exists
        groupRepository.findById(targetGroupUuid)
            .orElseThrow(() -> new GroupNotFoundException(targetGroupUuid));
        
        String userGroupKey = String.format(USER_GROUP_KEY, userId);
        
        // Get user's current group
        String currentGroupUuid = (String) redisTemplate.opsForValue().get(userGroupKey);
        
        if (currentGroupUuid != null) {
            // Remove from current group
            String currentMembershipKey = String.format(USER_MEMBERSHIP_KEY, currentGroupUuid);
            redisTemplate.opsForSet().remove(currentMembershipKey, userId);
        }
        
        // Add to target group
        String targetMembershipKey = String.format(USER_MEMBERSHIP_KEY, targetGroupUuid);
        redisTemplate.opsForSet().add(targetMembershipKey, userId);
        
        // Update user's current group
        redisTemplate.opsForValue().set(userGroupKey, targetGroupUuid);
        
        log.info("User {} moved from group {} to group {}", userId, currentGroupUuid, targetGroupUuid);
    }
    
    /**
     * Get all users in a group
     */
    public Set<String> getUsersInGroup(String groupUuid) {
        log.info("Fetching users in group {}", groupUuid);
        
        // Verify group exists
        groupRepository.findById(groupUuid)
            .orElseThrow(() -> new GroupNotFoundException(groupUuid));
        
        String membershipKey = String.format(USER_MEMBERSHIP_KEY, groupUuid);
        Set<Object> members = redisTemplate.opsForSet().members(membershipKey);
        
        Set<String> users = new HashSet<>();
        if (members != null) {
            members.forEach(member -> users.add(member.toString()));
        }
        
        return users;
    }
    
    /**
     * Map Group entity to GroupResponse DTO
     */
    private GroupResponse mapToResponse(Group group) {
        return GroupResponse.builder()
            .uuid(group.getUuid())
            .parentUuid(group.getParentUuid())
            .name(group.getName())
            .displayName(group.getDisplayName())
            .status(group.getStatus())
            .spaceId(group.getSpaceId())
            .location(group.getLocation())
            .language(group.getLanguage())
            .segments(group.getSegments())
            .build();
    }
}
