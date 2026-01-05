package com.muthukumaran.organization.controller;

import com.muthukumaran.organization.dto.*;
import com.muthukumaran.organization.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group Management", description = "APIs for managing groups and their hierarchy")
public class GroupController {
    
    private final GroupService groupService;
    
    @Operation(summary = "Create a new group", description = "Creates a new group with optional parent reference. Validates parent existence if provided.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Group created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Parent group not found")
    })
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupCreateRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "Get a group by UUID", description = "Retrieves a group with inherited properties from parent hierarchy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{uuid}")
    public ResponseEntity<GroupResponse> getGroup(
            @Parameter(description = "UUID of the group", required = true)
            @PathVariable String uuid) {
        GroupResponse response = groupService.getGroupWithInheritance(uuid);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update a group", description = "Updates an existing group's properties")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Group updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResponse.class))),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PutMapping("/{uuid}")
    public ResponseEntity<GroupResponse> updateGroup(
            @Parameter(description = "UUID of the group", required = true)
            @PathVariable String uuid,
            @Valid @RequestBody GroupUpdateRequest request) {
        GroupResponse response = groupService.updateGroup(uuid, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Delete a group", description = "Deletes a group if it has no child groups")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found"),
        @ApiResponse(responseCode = "409", description = "Group has child groups and cannot be deleted")
    })
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteGroup(
            @Parameter(description = "UUID of the group", required = true)
            @PathVariable String uuid) {
        groupService.deleteGroup(uuid);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Add a user to a group", description = "Adds a user to the specified group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User added successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{uuid}/users")
    public ResponseEntity<Void> addUserToGroup(
            @Parameter(description = "UUID of the group", required = true)
            @PathVariable String uuid,
            @Valid @RequestBody AddUserRequest request) {
        groupService.addUserToGroup(uuid, request.getUserId());
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Remove a user from a group", description = "Removes a user from the specified group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User removed successfully"),
        @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @DeleteMapping("/{uuid}/users/{userId}")
    public ResponseEntity<Void> removeUserFromGroup(
            @Parameter(description = "UUID of the group", required = true)
            @PathVariable String uuid,
            @Parameter(description = "ID of the user", required = true)
            @PathVariable String userId) {
        groupService.removeUserFromGroup(uuid, userId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get users in a group", description = "Retrieves all users in the specified group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{uuid}/users")
    public ResponseEntity<Set<String>> getUsersInGroup(
            @Parameter(description = "UUID of the group", required = true)
            @PathVariable String uuid) {
        Set<String> users = groupService.getUsersInGroup(uuid);
        return ResponseEntity.ok(users);
    }
}
