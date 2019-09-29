package com.watchers.repository;

import com.watchers.model.Tile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TileRepository extends CrudRepository<Tile, Long> {

}
