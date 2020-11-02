package com.watchers.service;

import com.watchers.manager.CleansingManager;
import com.watchers.manager.ContinentalDriftManager;
import com.watchers.manager.LifeManager;
import com.watchers.manager.MapManager;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.WorldTypeEnum;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.model.worldsetting.WorldSetting;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import com.watchers.repository.postgres.WorldRepositoryPersistent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
@EnableTransactionManagement
public class WorldService {

    private WorldRepositoryInMemory worldRepositoryInMemory;
    private WorldRepositoryPersistent worldRepositoryPersistent;
    private MapManager mapManager;
    private ContinentalDriftManager continentalDriftManager;
    private CleansingManager cleansingManager;
    private LifeManager lifeManager;
    private ArrayList<Long> activeWorldIds;

    @SuppressWarnings("unused")
    @Transactional("persistentDatabaseTransactionManager")
    public void saveAndShutdownAll(){
        activeWorldIds.stream().map(mapManager::getUninitiatedWorld).forEach(worldRepositoryPersistent::save);
        activeWorldIds.clear();
    }

    @SuppressWarnings("unused")
    public void saveAndShutdown(Long id){
        saveWorld(worldRepositoryInMemory.getOne(id));
        shutdownWorld(id);
    }

    public void shutdownWorld(Long id){
        activeWorldIds.stream()
                .filter(worldId -> worldId.equals(id))
                .findFirst()
                .ifPresent(activeWorldIds::remove);
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void saveWorlds(){
        activeWorldIds.stream()
                .map(mapManager::getInitiatedWorld)
                .filter(Objects::nonNull)
                .filter(world -> world.getContinents().stream().noneMatch(continent -> continent.getId() ==null))
                .filter(world -> world.getActorList().stream().noneMatch(actor -> actor.getId() == null))
                .filter(world -> world.getCoordinates().stream().noneMatch(coordinate -> coordinate.getTile().getBiome().getId() == null))
                .forEach(this::saveWorld
        );
    }

    @Transactional("persistentDatabaseTransactionManager")
    public void saveWorld(World memoryWorld){
        boolean exists = worldRepositoryPersistent.existsById(memoryWorld.getId());
        exists = false;
        if (!exists){
            worldCheckup(memoryWorld);
            worldRepositoryPersistent.save(memoryWorld);
            //persistenceSaveService.complexSaveToPersistence(memoryWorld);
            log.warn("The missing world is now saved to persistence.");
        } else {
            log.info("World is beeing updated from memory.");
            worldRepositoryPersistent.save(memoryWorld);
        }
    }

    public void processTurn(WorldTaskDto worldTaskDto){
        log.trace(getTotalHeight(worldTaskDto.getWorldId()));
        if(worldTaskDto.isContinentalshift()) {
            Assert.isTrue(worldTaskDto instanceof ContinentalDriftTaskDto, "The WorldTaskDto was initiated wrongly");
            continentalDriftManager.process((ContinentalDriftTaskDto) worldTaskDto);
            cleansingManager.process(worldTaskDto);
        }

        lifeManager.process(worldTaskDto);
        cleansingManager.process(worldTaskDto);
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    private String getTotalHeight(Long worldId) {
        World world = worldRepositoryInMemory.findById(worldId).get();
        long currentHeight = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .mapToLong(Tile::getHeight)
                .sum();
        long totalHeight = currentHeight + world.getHeightDeficit();
        return "Current height is: " +  totalHeight + ".";
    }

    /**
     * @param worldSetting the worldsettings of the world
     * @param startFromPersistents boolean value for the use of the persistent database
     * @return Boolean: true means added, null means already added false means not present in memory
     */
    public Boolean addActiveWorld(WorldSetting worldSetting, boolean startFromPersistents) {
        if (startFromPersistents){
            return addActiveWorldFromPersistence(worldSetting);
        } else {
            return addActiveWorldFromMemory(worldSetting);
        }
    }

    @Transactional("persistentDatabaseTransactionManager")
    private Boolean addActiveWorldFromPersistence(WorldSetting worldSetting) {
        Optional<World> optionalWorld = worldRepositoryPersistent.findById(worldSetting.getWorldId());
        if(optionalWorld.isPresent()) {
            World world = optionalWorld.get();
            worldCheckup(world);
            saveToMemory(world);
            if (!activeWorldIds.contains(worldSetting.getWorldId())) {
                activeWorldIds.add(worldSetting.getWorldId());
                log.info("World " + worldSetting.getWorldId() + " added as active world from the persistence database.");
                return true;
            } else {
                log.info("World " + worldSetting.getWorldId() + " added as active world from the persistence database.");
                return null;
            }
        } else {
            log.info("World " + worldSetting.getWorldId() + " was not present in the persistence database.");
            return false;
        }
    }

    private void worldCheckup(World world) {
        Assert.notNull(world, "The world was null.");
        world.fillTransactionals();
        Assert.isTrue(world.getContinents().stream().noneMatch(continent -> continent.getId() == null), "Some continents had nog id set.");
        Assert.isTrue(world.getActorList().stream().noneMatch(actor -> actor.getId() == null), "Some actors had no id set.");
        Assert.isTrue(world.getCoordinates().stream().noneMatch(coordinate -> coordinate.getTile().getBiome().getId() == null || coordinate.getTile().getBiome().getTile() == null), "Some biomes had nog id set.");
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    private void saveToMemory(World persistentWorld) {
        worldCheckup(persistentWorld);
        worldRepositoryInMemory.save(persistentWorld);
        //memorySaveService.complexSaveToMemory(persistentWorld);

        World newWorld = worldRepositoryInMemory.findById(persistentWorld.getId()).orElseThrow(() -> new RuntimeException("The world was lost in perstistence."));
        log.info("current coordinates from memory are: " + newWorld.getCoordinates().size());
        log.info("current coordinates from memory are: " + persistentWorld.getCoordinates().size());
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    private Boolean addActiveWorldFromMemory(WorldSetting worldSetting) {
        if (worldRepositoryInMemory.existsById(worldSetting.getWorldId())) {
            if (!activeWorldIds.contains(worldSetting.getWorldId())) {
                activeWorldIds.add(worldSetting.getWorldId());
                log.info("The requested world " + worldSetting.getWorldId() + " is now active.");
                return true;
            } else {
                log.info("The requested world " + worldSetting.getWorldId() + " was alreadu active.");
                return null;
            }
        } else {
            log.warn("The world " + worldSetting.getWorldId() + " does not exist in the persistence context. A new world is going to be created. Large worlds take a while being generated.");
            World world = mapManager.createWorld(worldSetting);
            worldRepositoryInMemory.save(world);
            log.info("Created a new world! Number: " + world.getId());
            activeWorldIds.add(worldSetting.getWorldId());
            return true;
        }
    }
}
