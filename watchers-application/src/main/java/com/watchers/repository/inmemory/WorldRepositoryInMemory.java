package com.watchers.repository.inmemory;

import com.watchers.model.world.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldRepositoryInMemory extends JpaRepository<World, Long>, JpaSpecificationExecutor<World> {
}
