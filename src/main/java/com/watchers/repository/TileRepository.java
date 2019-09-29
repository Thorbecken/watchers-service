package com.watchers.repository;

import com.watchers.model.Tile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TileRepository extends CrudRepository<Tile, Long> {

/*    @Query("SELECT t FROM Tile t WHERE t.worldId = :worldId")
    List<Tile> getWorldTiles(@Param("worldId") long worldId);*/

}
