package com.muthukumaran.organization.controller;

import com.muthukumaran.organization.dto.MoveUserRequest;
import com.muthukumaran.organization.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user memberships")
public class UserController {
    
    private final GroupService groupService;
    
    @Operation(summary = "Move a user to another group", 
               description = "Atomically moves a user from their current group to a target group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User moved successfully"),
        @ApiResponse(responseCode = "404", description = "Target group not found")
    })
    @PutMapping("/{userId}/move")
    public ResponseEntity<Void> moveUser(
            @Parameter(description = "ID of the user to move", required = true)
            @PathVariable String userId,
            @Valid @RequestBody MoveUserRequest request) {
        groupService.moveUser(userId, request.getTargetGroupUuid());
        return ResponseEntity.ok().build();
    }
}
