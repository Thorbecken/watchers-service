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

        long xSize = 10L;
        long ySize = 10L;

        World world = new World();

        Set<Tile> worldTiles = new HashSet<>();
        for (long xCoord = 1; xCoord <= xSize; xCoord++){
            for (long yCoord = 1; yCoord <= ySize; yCoord++){
                float r = rand.nextFloat();
                float g = rand.nextFloat();
                float b = rand.nextFloat();

                Color color = new Color(r,g,b);
                Tile tile = new Tile(xCoord,yCoord, color, world);
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
