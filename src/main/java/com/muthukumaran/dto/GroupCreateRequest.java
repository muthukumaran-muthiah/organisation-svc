package com.muthukumaran.organization.dto;

import com.muthukumaran.organization.model.GroupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new group")
public class GroupCreateRequest {
    
    @Schema(description = "Reference to parent group UUID (nullable)", example = "123e4567-e89b-12d3-a456-426614174000")
    private String parentUuid;
    
    @NotBlank(message = "Name is mandatory")
    @Schema(description = "Internal name of the group", example = "Engineering Team", required = true)
    private String name;
    
    @Schema(description = "Public display name (defaults to name if not provided)", example = "Engineering Team - US")
    private String displayName;
    
    @Schema(description = "Group status (defaults to ACTIVE)", example = "ACTIVE")
    private GroupStatus status;
    
    @Schema(description = "ID of the linked Client Space", example = "space-123")
    private String spaceId;
    
    @Schema(description = "Location of the group", example = "USA")
    private String location;
    
    @Schema(description = "Language code", example = "en-US")
    private String language;
    
    @Schema(description = "List of business verticals", example = "[\"Corporate\", \"Education\"]")
    private List<String> segments;
}
