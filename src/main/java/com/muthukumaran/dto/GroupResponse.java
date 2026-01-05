package com.muthukumaran.organization.dto;

import com.muthukumaran.organization.model.GroupStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for a group with inherited properties")
public class GroupResponse {
    
    @Schema(description = "Unique identifier of the group", example = "123e4567-e89b-12d3-a456-426614174000")
    private String uuid;
    
    @Schema(description = "Parent group UUID", example = "parent-uuid-123")
    private String parentUuid;
    
    @Schema(description = "Internal name of the group", example = "Engineering Team")
    private String name;
    
    @Schema(description = "Public display name", example = "Engineering Team - US")
    private String displayName;
    
    @Schema(description = "Group status", example = "ACTIVE")
    private GroupStatus status;
    
    @Schema(description = "ID of the linked Client Space (inherited if null)", example = "space-123")
    private String spaceId;
    
    @Schema(description = "Location of the group (inherited if null)", example = "USA")
    private String location;
    
    @Schema(description = "Language code (inherited if null)", example = "en-US")
    private String language;
    
    @Schema(description = "List of business verticals (inherited if null)", example = "[\"Corporate\", \"Education\"]")
    private List<String> segments;
}
