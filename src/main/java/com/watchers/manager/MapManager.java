package com.watchers.manager;

import com.watchers.helper.RandomHelper;
import com.watchers.model.actor.AnimalType;
import com.watchers.model.actor.animals.AnimalFactory;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
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
       world.ifPresent(World::fillTransactionals);

        return world.orElseGet(() -> createWorld(worldId));
    }
    
    private World createWorld(long worldId){
        World newWorld = new WorldFactory().generateWorld(58L, 28L, 13);

        log.info(String.format("World number %s created", worldId));
        worldRepository.save(newWorld);
        return newWorld;
    }

    public void seedLife(World world, Long xCoord, Long yCoord) {
        Tile seedingTile = world.getTile(xCoord, yCoord);
        AnimalType animalType = selectAnimalSeed(seedingTile.getContinent().getType());
        seedingTile.getActors().add(AnimalFactory.generateNewAnimal(animalType, seedingTile));
    }

    private AnimalType selectAnimalSeed(SurfaceType type) {
        switch (type){
            case CONTINENTAL: return AnimalType.RABBIT;
            case COASTAL:
            case OCEANIC:
            case DEEP_OCEAN:
                return AnimalType.WHALE;
            default: return AnimalType.RABBIT;
        }
    }
}
