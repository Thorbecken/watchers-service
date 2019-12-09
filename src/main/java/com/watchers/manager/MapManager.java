package com.watchers.manager;

import com.watchers.model.Tile;
import com.watchers.model.World;
import com.watchers.repository.TileRepository;
import com.watchers.repository.WorldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;

@Slf4j
@Service
public class MapManager {

    @Autowired
    private WorldRepository worldRepository;

    @Autowired
    private TileRepository tileRepository;
    
    public World getWorld(Long worldId) {
       Optional<World> world = worldRepository.findById(worldId);

        return world.orElseGet(() -> createWorld(worldId));
    }
    
    //@Transactional(propagation = Propagation.REQUIRED)
    public World createWorld(long worldId){
        Random rand = new Random();

        World world = new World();
        world.setxSize(58L);
        world.setySize(28L);


        Set<Tile> worldTiles = new HashSet<>();
        for (long xCoord = 1; xCoord <= world.getxSize(); xCoord++){
            for (long yCoord = 1; yCoord <= world.getySize(); yCoord++){
                Tile tile = new Tile(xCoord, yCoord, world);

                tile.setLandType("water");

                tileRepository.save(tile);
                worldTiles.add(tile);
            }
        }

        world.setTiles(worldTiles);

        log.info(String.format("World number %s created", worldId));
        worldRepository.save(world);
        
        return world;
    }
}
