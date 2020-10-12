package com.watchers.manager;

import com.watchers.components.continentaldrift.ContinentalDriftDirectionChanger;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

@Slf4j
@Service
@EnableTransactionManagement
public class MapManager {

    private long xSize;
    private long ySize;
    private int numberOfContinents;
    private WorldRepositoryInMemory worldRepositoryInMemory;
    private WorldFactory worldFactory;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;

    public MapManager(@Value("${watch.worldGeneration.xSize}") long xSize,@Value("${watch.worldGeneration.ySize}") long ySize,@Value("${watch.worldGeneration.numberOfContinents}") int numberOfContinents, WorldRepositoryInMemory worldRepositoryInMemory,
                      WorldFactory worldFactory,
                      ContinentalDriftDirectionChanger continentalDriftDirectionChanger){
        this.xSize = xSize;
        this.ySize = ySize;
        this.numberOfContinents = numberOfContinents;
        this.worldRepositoryInMemory = worldRepositoryInMemory;
        this.worldFactory = worldFactory;
        this.continentalDriftDirectionChanger = continentalDriftDirectionChanger;
    }

    public World getInitiatedWorld(Long worldId){
        return getWorld(worldId, true);
    }

    public World getUninitiatedWorld(Long worldId){
        return getWorld(worldId, false);
    }

    public World getWorld(Long worldId, boolean initiated) {
       World world = worldRepositoryInMemory.findById(worldId).orElseGet(() -> createWorld(worldId));
        log.trace("world loaden from memory with: "+ (world.getCoordinates().stream().map(Coordinate::getTile).map(Tile::getHeight).reduce(0L, (x, y) -> x+y) + world.getHeightDeficit()) + " height");
        log.trace("the loaded world contains: " + world.getCoordinates().size() + " number of coordinates");
       if(initiated) {
           world.fillTransactionals();
       }

        Assert.isTrue(world.getCoordinates().size() == world.getXSize()*world.getYSize(), "coordinates were " +world.getCoordinates().size());
        return world;
    }

    private World createWorld(long worldId){
        World newWorld = worldFactory.generateWorld(xSize, ySize, numberOfContinents);
        log.info(String.format("World number %s created", worldId));
        worldRepositoryInMemory.save(newWorld);
        continentalDriftDirectionChanger.assignFirstOrNewDriftDirections(newWorld);
        worldRepositoryInMemory.save(newWorld);
        Assert.isTrue(newWorld.getCoordinates().size() == newWorld.getXSize()*newWorld.getYSize(), "coordinates were " +newWorld.getCoordinates().size());
        return newWorld;
    }
}
