package com.watchers.repository.postgres;

import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinateRepositoryPersistent extends JpaRepository<Coordinate, Long>, JpaSpecificationExecutor<Coordinate> {
        }