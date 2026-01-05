package com.muthukumaran.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to move a user to another group")
public class MoveUserRequest {
    
    @NotBlank(message = "Target group UUID is mandatory")
    @Schema(description = "UUID of the target group", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private String targetGroupUuid;
}
