package com.watchers.repository.inmemory;

import com.watchers.model.common.Direction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DirectionRepositoryInMemory extends JpaRepository<Direction, Long>, JpaSpecificationExecutor<Direction> {
}
