package com.watchers.repository.inmemory;

import com.watchers.model.common.Coordinate;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface CoordinateRepositoryInMemory extends CrudRepository<Coordinate, Long>, JpaSpecificationExecutor<Coordinate> {
}
