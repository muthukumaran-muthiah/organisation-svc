package com.muthukumaran.organization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Group")
public class Group {
    
    @Id
    @Indexed
    private String uuid;
    
    @Indexed
    private String parentUuid;
    
    private String name;
    
    private String displayName;
    
    private GroupStatus status;
    
    private String spaceId;
    
    private String location;
    
    private String language;
    
    private List<String> segments;
    
    public String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }
}
