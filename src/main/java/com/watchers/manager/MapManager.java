package com.watchers.manager;

import com.watchers.components.continentaldrift.ContinentalDriftDirectionAdjuster;
import com.watchers.model.actor.AnimalType;
import com.watchers.model.actor.animals.AnimalFactory;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MapManager {

    private WorldRepository worldRepository;
    private WorldFactory worldFactory;
    private ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster;

    public MapManager(WorldRepository worldRepository,
                      WorldFactory worldFactory,
                      ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster){
        this.worldRepository = worldRepository;
        this.worldFactory = worldFactory;
        this.continentalDriftDirectionAdjuster = continentalDriftDirectionAdjuster;
    }

    public World getWorld(Long worldId) {
       Optional<World> world = worldRepository.findById(worldId);
       world.ifPresent(World::fillTransactionals);

        return world.orElseGet(() -> createWorld(worldId));
    }
    
    private World createWorld(long worldId){
        World newWorld = worldFactory.generateWorld(58L, 28L, 13);
        log.info(String.format("World number %s created", worldId));
        worldRepository.save(newWorld);
        continentalDriftDirectionAdjuster.assignFirstOrNewDriftDirections(newWorld);
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
            case PLAIN: return AnimalType.RABBIT;
            case COASTAL:
            case OCEANIC:
            case DEEP_OCEAN:
                return AnimalType.WHALE;
            default: return AnimalType.RABBIT;
        }
    }
}
