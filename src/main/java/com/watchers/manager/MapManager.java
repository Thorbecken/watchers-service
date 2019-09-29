package com.watchers.manager;

import com.watchers.model.Coordinate;
import com.watchers.model.Tile;
import com.watchers.model.World;
import com.watchers.repository.TileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//@Slf4j
@Component
public class MapManager {

    /*private TileRepository tileRepository;

    @Autowired
    public MapManager(TileRepository tileRepository){
        this.tileRepository = tileRepository;
    }


    public World getWorld(long worldId) {
        World world = new World();
        Map<Coordinate, Tile> tileMap = new HashMap<>();
        List<Tile> worldTiles = tileRepository.getWorldTiles(worldId);

        if(worldTiles.isEmpty()){
            return createWorld(worldId);
        }

        worldTiles.forEach(tile -> tileMap.put(tile.getCoordinate(), tile));

        return world;
    }

    public World createWorld(long worldId){
        Random rand = new Random();

        long xSize = 10L;
        long ySize = 10L;

        Map<Coordinate, Tile> worldTiles = new HashMap<>();
        for (long xCoord = 1; xCoord <= xSize; xCoord++){
            for (long yCoord = 1; yCoord <= ySize; yCoord++){
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();

                Color color = new Color(r,g,b);
                Tile tile = new Tile(xCoord,yCoord, color, worldId);
                worldTiles.put(tile.getCoordinate(), tile);
            }
        }

        World world = new World();
        world.setTileMap(worldTiles);

        //log.info(String.format("World number %s created", worldId));
        return world;
    }*/
}
