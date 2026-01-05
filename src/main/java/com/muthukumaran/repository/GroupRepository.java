package com.muthukumaran.organization.repository;

import com.muthukumaran.organization.model.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends CrudRepository<Group, String> {
    
    List<Group> findByParentUuid(String parentUuid);
}
