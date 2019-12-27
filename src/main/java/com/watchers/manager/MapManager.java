package com.watchers.manager;

import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Slf4j
@Service
public class MapManager {

    @Autowired
    private WorldRepository worldRepository;

    public World getWorld(Long worldId) {
       Optional<World> world = worldRepository.findById(worldId);

        return world.orElseGet(() -> createWorld(worldId));
    }
    
    private World createWorld(long worldId){
        World newWorld = new WorldFactory().generateWorld(58L, 28L, 13);

        log.info(String.format("World number %s created", worldId));
        worldRepository.save(newWorld);
        
        return newWorld;
    }
}
