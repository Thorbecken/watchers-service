package com.watchers.repository.postgres;

import com.watchers.model.environment.World;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldRepositoryPersistent extends JpaRepository<World, Long> {
        }