package com.watchers.service;

import com.watchers.config.SettingConfiguration;
import com.watchers.manager.CleansingManager;
import com.watchers.manager.ContinentalDriftManager;
import com.watchers.manager.LifeManager;
import com.watchers.manager.MapManager;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.*;
import com.watchers.repository.postgres.WorldRepositoryPersistent;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
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
    private SettingConfiguration settingConfiguration;
    private List<Long> activeWorldIds = new ArrayList<>();

    @SuppressWarnings("unused")
    @Transactional("persistentDatabaseTransactionManager")
    public void saveAndShutdownAll(){
        activeWorldIds.stream().map(mapManager::getUninitiatedWorld).forEach(worldRepositoryPersistent::save);
        activeWorldIds.clear();
    }

    @SuppressWarnings("unused")
    public void saveAndShutdown(Long id){
        saveWorld(null);
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
    public void saveWorld(@NonNull World memoryWorld){
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

    public void processTurns(){
        activeWorldIds.forEach(this::processTurn);
    }

    private void processTurn(Long worldId){
        ContinentalDriftTaskDto continentalDriftTaskDto = new ContinentalDriftTaskDto(worldId, false, true, settingConfiguration.getHeigtDivider(), settingConfiguration.getMinimumContinents());
        continentalDriftManager.process(continentalDriftTaskDto);
        cleansingManager.process(continentalDriftTaskDto);
        lifeManager.process(continentalDriftTaskDto);

        WorldTaskDto worldTaskDto = new WorldTaskDto(worldId, false, true);
        cleansingManager.process(worldTaskDto);
    }

    public void processTurn(WorldTaskDto worldTaskDto){
        if(worldTaskDto.isContinentalshift()) {
            Assert.isTrue(worldTaskDto instanceof ContinentalDriftTaskDto, "The WorldTaskDto was initiated wrongly");
            continentalDriftManager.process((ContinentalDriftTaskDto) worldTaskDto);
            cleansingManager.process(worldTaskDto);
        }

        lifeManager.process(worldTaskDto);
        cleansingManager.process(worldTaskDto);
    }

    public void executeTurn() {
        processTurns();

        log.info("Processed a turn");
    }

    /**
     * @param id the id of the world to be added
     * @param startFromPersistents boolean value for the use of the persistent database
     * @return Boolean: true means added, null means already added false means not present in memory
     */
    public Boolean addActiveWorld(Long id, boolean startFromPersistents) {
        if (startFromPersistents){
            return addActiveWorldFromPersistence(id);
        } else {
            return addActiveWorldFromMemory(id);
        }
    }

    @Transactional("persistentDatabaseTransactionManager")
    private Boolean addActiveWorldFromPersistence(Long id) {
        Optional<World> optionalWorld = worldRepositoryPersistent.findById(id);
        if(optionalWorld.isPresent()) {
            World world = optionalWorld.get();
            worldCheckup(world);
            saveToMemory(world);
            if (!activeWorldIds.contains(id)) {
                activeWorldIds.add(id);
                log.info("World " + id + " added as active world from the persistence database.");
                return true;
            } else {
                log.info("World " + id + " added as active world from the persistence database.");
                return null;
            }
        } else {
            log.info("World " + id + " was not present in the persistence database.");
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

    private Boolean addActiveWorldFromMemory(Long id) {
        if (worldRepositoryInMemory.existsById(id)) {
            if (!activeWorldIds.contains(id)) {
                activeWorldIds.add(id);
                log.info("The requested world " + id + " is now active.");
                return true;
            } else {
                log.info("The requested world " + id + " was alreadu active.");
                return null;
            }
        } else {
            log.warn("The world " + id + " does not exist in the persistence context. A new world is created.");
            mapManager.getWorld(id, false);
            activeWorldIds.add(id);
            return true;
        }
    }
}
