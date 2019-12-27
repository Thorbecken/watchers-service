package com.watchers.repository;

import com.watchers.model.environment.Tile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TileRepository extends CrudRepository<Tile, Long> {

}
