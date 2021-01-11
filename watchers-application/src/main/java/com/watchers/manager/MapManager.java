package com.watchers.manager;

import com.watchers.config.SettingConfiguration;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.WorldTypeEnum;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.model.worldsetting.WorldSetting;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@EnableTransactionManagement
public class MapManager {

    private WorldRepositoryInMemory worldRepositoryInMemory;
    private WorldFactory worldFactory;
    private SettingConfiguration settingConfiguration;

    public World getInitiatedWorld(Long worldId){
        return getWorld(worldId, true);
    }

    public World getUninitiatedWorld(Long worldId){
        return getWorld(worldId, false);
    }

    public World getWorld(Long worldId, boolean initiated) {
       Optional<World> optionalWorld = worldRepositoryInMemory.findById(worldId);
       if(optionalWorld.isPresent()) {
           World world = optionalWorld.get();
           log.trace("world loaden from memory with: " + (world.getCoordinates().stream().map(Coordinate::getTile).map(Tile::getHeight).reduce(0L, (x, y) -> x + y) + world.getHeightDeficit()) + " height");
           log.trace("the loaded world contains: " + world.getCoordinates().size() + " number of coordinates");
           if (initiated) {
               world.fillTransactionals();
           }

           Assert.isTrue(world.getCoordinates().size() == world.getXSize() * world.getYSize(), "Coordinate size was expected to be " + world.getXSize() * world.getYSize() + ", but were " + world.getCoordinates().size());
           return world;
       } else {
           return null;
       }
    }

    public World createWorld(WorldSetting worldSetting){
        World newWorld = worldFactory.generateWorld(settingConfiguration.getXSize(), settingConfiguration.getYSize(), settingConfiguration.getMinimumContinents(), worldSetting);
        Assert.isTrue(newWorld.getCoordinates().size() == newWorld.getXSize()*newWorld.getYSize(), "coordinates were " + newWorld.getCoordinates().size());
        return newWorld;
    }
}
