package com.watchers.repository.inMemory;

import com.watchers.model.environment.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorldRepositoryInMemory extends JpaRepository<World, Long>, JpaSpecificationExecutor<World> {
}
