package com.muthukumaran.organization;

import com.muthukumaran.organization.dto.GroupCreateRequest;
import com.muthukumaran.organization.dto.GroupResponse;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for verifying the inheritance logic of the Organization Service.
 * This test ensures that child groups correctly inherit properties from their parent hierarchy.
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InheritanceIntegrationTest {
    
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
    
    private String rootGroupUuid;
    private String parentGroupUuid;
    private String childGroupUuid;
    
    @Test
    @Order(1)
    @DisplayName("Should create root group with all properties defined")
    void testCreateRootGroup() {
        // Given: A root group with all properties defined
        GroupCreateRequest rootRequest = GroupCreateRequest.builder()
                .name("Root Organization")
                .displayName("Root Organization")
                .status(GroupStatus.ACTIVE)
                .spaceId("space-root")
                .location("USA")
                .language("en-US")
                .segments(Arrays.asList("Corporate", "Education"))
                .build();
        
        // When: Creating the root group
        GroupResponse rootGroup = groupService.createGroup(rootRequest);
        
        // Then: Root group should be created with all properties
        assertNotNull(rootGroup);
        assertNotNull(rootGroup.getUuid());
        assertEquals("Root Organization", rootGroup.getName());
        assertEquals("space-root", rootGroup.getSpaceId());
        assertEquals("USA", rootGroup.getLocation());
        assertEquals("en-US", rootGroup.getLanguage());
        assertEquals(2, rootGroup.getSegments().size());
        
        rootGroupUuid = rootGroup.getUuid();
    }
    
    @Test
    @Order(2)
    @DisplayName("Should create parent group with partial properties")
    void testCreateParentGroup() {
        // Given: A parent group with only some properties defined (to be inherited from root)
        GroupCreateRequest parentRequest = GroupCreateRequest.builder()
                .parentUuid(rootGroupUuid)
                .name("Engineering Department")
                .displayName("Engineering Department")
                .status(GroupStatus.ACTIVE)
                .spaceId("space-eng")
                // No location - should inherit from root
                .language("en-US")
                // No segments - should inherit from root
                .build();
        
        // When: Creating the parent group
        GroupResponse parentGroup = groupService.createGroup(parentRequest);
        
        // Then: Parent group should be created
        assertNotNull(parentGroup);
        assertNotNull(parentGroup.getUuid());
        assertEquals("Engineering Department", parentGroup.getName());
        assertEquals("space-eng", parentGroup.getSpaceId());
        assertNull(parentGroup.getLocation()); // Not inherited during creation
        assertEquals("en-US", parentGroup.getLanguage());
        assertNull(parentGroup.getSegments()); // Not inherited during creation
        
        parentGroupUuid = parentGroup.getUuid();
    }
    
    @Test
    @Order(3)
    @DisplayName("Should create child group with minimal properties")
    void testCreateChildGroup() {
        // Given: A child group with only name defined
        GroupCreateRequest childRequest = GroupCreateRequest.builder()
                .parentUuid(parentGroupUuid)
                .name("Frontend Team")
                .displayName("Frontend Team")
                .status(GroupStatus.ACTIVE)
                // No spaceId, location, language, segments - should inherit from parent/root
                .build();
        
        // When: Creating the child group
        GroupResponse childGroup = groupService.createGroup(childRequest);
        
        // Then: Child group should be created
        assertNotNull(childGroup);
        assertNotNull(childGroup.getUuid());
        assertEquals("Frontend Team", childGroup.getName());
        assertNull(childGroup.getSpaceId()); // Not inherited during creation
        assertNull(childGroup.getLocation()); // Not inherited during creation
        assertNull(childGroup.getLanguage()); // Not inherited during creation
        assertNull(childGroup.getSegments()); // Not inherited during creation
        
        childGroupUuid = childGroup.getUuid();
    }
    
    @Test
    @Order(4)
    @DisplayName("Should inherit properties when retrieving child group")
    void testInheritanceLogic() {
        // When: Retrieving the child group with inheritance
        GroupResponse childGroup = groupService.getGroupWithInheritance(childGroupUuid);
        
        // Then: Child group should have inherited properties from parent and root
        assertNotNull(childGroup);
        assertEquals("Frontend Team", childGroup.getName());
        
        // spaceId should be inherited from parent (space-eng)
        assertEquals("space-eng", childGroup.getSpaceId());
        
        // location should be inherited from root (USA) since parent doesn't have it
        assertEquals("USA", childGroup.getLocation());
        
        // language should be inherited from parent (en-US)
        assertEquals("en-US", childGroup.getLanguage());
        
        // segments should be inherited from root (Corporate, Education) since parent doesn't have it
        assertNotNull(childGroup.getSegments());
        assertEquals(2, childGroup.getSegments().size());
        assertTrue(childGroup.getSegments().contains("Corporate"));
        assertTrue(childGroup.getSegments().contains("Education"));
    }
    
    @Test
    @Order(5)
    @DisplayName("Should inherit properties when retrieving parent group")
    void testParentGroupInheritance() {
        // When: Retrieving the parent group with inheritance
        GroupResponse parentGroup = groupService.getGroupWithInheritance(parentGroupUuid);
        
        // Then: Parent group should have inherited missing properties from root
        assertNotNull(parentGroup);
        assertEquals("Engineering Department", parentGroup.getName());
        
        // spaceId is defined in parent
        assertEquals("space-eng", parentGroup.getSpaceId());
        
        // location should be inherited from root
        assertEquals("USA", parentGroup.getLocation());
        
        // language is defined in parent
        assertEquals("en-US", parentGroup.getLanguage());
        
        // segments should be inherited from root
        assertNotNull(parentGroup.getSegments());
        assertEquals(2, parentGroup.getSegments().size());
        assertTrue(parentGroup.getSegments().contains("Corporate"));
        assertTrue(parentGroup.getSegments().contains("Education"));
    }
    
    @Test
    @Order(6)
    @DisplayName("Root group should not inherit any properties")
    void testRootGroupNoInheritance() {
        // When: Retrieving the root group
        GroupResponse rootGroup = groupService.getGroupWithInheritance(rootGroupUuid);
        
        // Then: Root group should have only its own properties (no inheritance)
        assertNotNull(rootGroup);
        assertEquals("Root Organization", rootGroup.getName());
        assertEquals("space-root", rootGroup.getSpaceId());
        assertEquals("USA", rootGroup.getLocation());
        assertEquals("en-US", rootGroup.getLanguage());
        assertNotNull(rootGroup.getSegments());
        assertEquals(2, rootGroup.getSegments().size());
    }
}
