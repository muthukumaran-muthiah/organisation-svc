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
@Schema(description = "Request to add a user to a group")
public class AddUserRequest {
    
    @NotBlank(message = "User ID is mandatory")
    @Schema(description = "ID of the user to add", example = "user-123", required = true)
    private String userId;
}
