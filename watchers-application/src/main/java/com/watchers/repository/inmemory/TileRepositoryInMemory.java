package com.watchers.repository.inmemory;

import com.watchers.model.environment.Tile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TileRepositoryInMemory extends JpaRepository<Tile, Long>, JpaSpecificationExecutor<Tile> {
}