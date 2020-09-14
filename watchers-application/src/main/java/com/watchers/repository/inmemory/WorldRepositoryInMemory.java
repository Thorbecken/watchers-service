package com.watchers.repository.inmemory;

import com.watchers.model.environment.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldRepositoryInMemory extends CrudRepository<World, Long>, JpaSpecificationExecutor<World> {
}
