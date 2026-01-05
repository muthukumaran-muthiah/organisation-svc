package com.muthukumaran.organization;

import com.muthukumaran.organization.dto.*;
import com.muthukumaran.organization.exception.GroupHasChildrenException;
import com.muthukumaran.organization.exception.GroupNotFoundException;
import com.muthukumaran.organization.exception.ParentGroupNotFoundException;
import com.muthukumaran.organization.model.GroupStatus;
import com.muthukumaran.organization.service.GroupService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for the Organization Service
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrganizationServiceIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
    
    @Autowired
    private GroupService groupService;
    
    private String testGroupUuid;
    private String childGroupUuid;
    
    @Test
    @Order(1)
    @DisplayName("Should create a group with default status ACTIVE")
    void testCreateGroupWithDefaultStatus() {
        GroupCreateRequest request = GroupCreateRequest.builder()
                .name("Test Group")
                .build();
        
        GroupResponse response = groupService.createGroup(request);
        
        assertNotNull(response);
        assertNotNull(response.getUuid());
        assertEquals("Test Group", response.getName());
        assertEquals("Test Group", response.getDisplayName()); // Should default to name
        assertEquals(GroupStatus.ACTIVE, response.getStatus());
        
        testGroupUuid = response.getUuid();
    }
    
    @Test
    @Order(2)
    @DisplayName("Should fail to create group with non-existent parent")
    void testCreateGroupWithInvalidParent() {
        GroupCreateRequest request = GroupCreateRequest.builder()
                .parentUuid("non-existent-uuid")
                .name("Invalid Group")
                .build();
        
        assertThrows(ParentGroupNotFoundException.class, () -> {
            groupService.createGroup(request);
        });
    }
    
    @Test
    @Order(3)
    @DisplayName("Should create child group with valid parent")
    void testCreateChildGroup() {
        GroupCreateRequest request = GroupCreateRequest.builder()
                .parentUuid(testGroupUuid)
                .name("Child Group")
                .displayName("Child Group Display")
                .location("France")
                .build();
        
        GroupResponse response = groupService.createGroup(request);
        
        assertNotNull(response);
        assertEquals("Child Group", response.getName());
        assertEquals(testGroupUuid, response.getParentUuid());
        assertEquals("France", response.getLocation());
        
        childGroupUuid = response.getUuid();
    }
    
    @Test
    @Order(4)
    @DisplayName("Should update group properties")
    void testUpdateGroup() {
        GroupUpdateRequest request = GroupUpdateRequest.builder()
                .name("Updated Group")
                .location("Germany")
                .segments(Arrays.asList("Healthcare", "Finance"))
                .build();
        
        GroupResponse response = groupService.updateGroup(testGroupUuid, request);
        
        assertEquals("Updated Group", response.getName());
        assertEquals("Germany", response.getLocation());
        assertEquals(2, response.getSegments().size());
    }
    
    @Test
    @Order(5)
    @DisplayName("Should fail to delete group with children")
    void testDeleteGroupWithChildren() {
        assertThrows(GroupHasChildrenException.class, () -> {
            groupService.deleteGroup(testGroupUuid);
        });
    }
    
    @Test
    @Order(6)
    @DisplayName("Should add user to group")
    void testAddUserToGroup() {
        groupService.addUserToGroup(testGroupUuid, "user-123");
        
        Set<String> users = groupService.getUsersInGroup(testGroupUuid);
        assertTrue(users.contains("user-123"));
    }
    
    @Test
    @Order(7)
    @DisplayName("Should move user between groups")
    void testMoveUser() {
        groupService.addUserToGroup(testGroupUuid, "user-456");
        
        // Verify user is in the first group
        Set<String> usersBeforeMove = groupService.getUsersInGroup(testGroupUuid);
        assertTrue(usersBeforeMove.contains("user-456"));
        
        // Move user to child group
        groupService.moveUser("user-456", childGroupUuid);
        
        // Verify user is no longer in the first group
        Set<String> usersAfterMove = groupService.getUsersInGroup(testGroupUuid);
        assertFalse(usersAfterMove.contains("user-456"));
        
        // Verify user is in the child group
        Set<String> childUsers = groupService.getUsersInGroup(childGroupUuid);
        assertTrue(childUsers.contains("user-456"));
    }
    
    @Test
    @Order(8)
    @DisplayName("Should remove user from group")
    void testRemoveUserFromGroup() {
        groupService.addUserToGroup(testGroupUuid, "user-789");
        
        // Verify user is in the group
        Set<String> usersBefore = groupService.getUsersInGroup(testGroupUuid);
        assertTrue(usersBefore.contains("user-789"));
        
        // Remove user
        groupService.removeUserFromGroup(testGroupUuid, "user-789");
        
        // Verify user is no longer in the group
        Set<String> usersAfter = groupService.getUsersInGroup(testGroupUuid);
        assertFalse(usersAfter.contains("user-789"));
    }
    
    @Test
    @Order(9)
    @DisplayName("Should delete group without children")
    void testDeleteGroupWithoutChildren() {
        // Delete child group first (it has no children)
        groupService.deleteGroup(childGroupUuid);
        
        // Verify it's deleted
        assertThrows(GroupNotFoundException.class, () -> {
            groupService.getGroupWithInheritance(childGroupUuid);
        });
        
        // Now parent group can be deleted
        groupService.deleteGroup(testGroupUuid);
        
        // Verify it's deleted
        assertThrows(GroupNotFoundException.class, () -> {
            groupService.getGroupWithInheritance(testGroupUuid);
        });
    }
    
    @Test
    @Order(10)
    @DisplayName("Should fail to get non-existent group")
    void testGetNonExistentGroup() {
        assertThrows(GroupNotFoundException.class, () -> {
            groupService.getGroupWithInheritance("non-existent-uuid");
        });
    }
}
