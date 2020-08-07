package com.watchers.manager;

import com.watchers.components.continentaldrift.ContinentalDriftDirectionAdjuster;
import com.watchers.model.actor.AnimalType;
import com.watchers.model.actor.animals.AnimalFactory;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@EnableTransactionManagement
public class MapManager {

    private WorldRepositoryInMemory worldRepositoryInMemory;
    private WorldFactory worldFactory;
    private ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster;

    public MapManager(WorldRepositoryInMemory worldRepositoryInMemory,
                      WorldFactory worldFactory,
                      ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster){
        this.worldRepositoryInMemory = worldRepositoryInMemory;
        this.worldFactory = worldFactory;
        this.continentalDriftDirectionAdjuster = continentalDriftDirectionAdjuster;
    }

    public World getInitiatedWorld(Long worldId){
        return getWorld(worldId, true);
    }

    public World getUninitiatedWorld(Long worldId){
        return getWorld(worldId, false);
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public World getWorld(Long worldId, boolean initiated) {
       World world = worldRepositoryInMemory.findById(worldId).orElseGet(() -> createWorld(worldId));

       if(initiated) {
           world.fillTransactionals();
       }

        return world;
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    private World createWorld(long worldId){
        World newWorld = worldFactory.generateWorld(58L, 28L, 13);
        log.info(String.format("World number %s created", worldId));
        worldRepositoryInMemory.save(newWorld);
        continentalDriftDirectionAdjuster.assignFirstOrNewDriftDirections(newWorld);
        worldRepositoryInMemory.save(newWorld);
        return newWorld;
    }

    public void seedLife(World world, Long xCoord, Long yCoord) {
        Tile seedingTile = world.getTile(xCoord, yCoord);
        AnimalType animalType = selectAnimalSeed(seedingTile.getContinent().getType());
        seedingTile.getActors().add(AnimalFactory.generateNewAnimal(animalType, seedingTile));
        worldRepositoryInMemory.save(world);
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
