package com.watchers.repository;

import com.watchers.model.environment.World;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldRepository extends CrudRepository<World, Long> {

}
