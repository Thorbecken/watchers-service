package com.watchers.service;

import com.watchers.components.WorldCleanser;
import com.watchers.manager.ContinentalDriftManager;
import com.watchers.manager.MapManager;
import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import com.watchers.repository.postgres.WorldRepositoryPersistent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableTransactionManagement
public class WorldService {

    private WorldRepositoryInMemory worldRepositoryInMemory;
    private WorldRepositoryPersistent worldRepositoryPersistent;
    private MapManager mapManager;
    private ContinentalDriftManager continentalDriftManager;
    private WorldCleanser worldCleanser;
    private List<Long> activeWorldIds = new ArrayList<>();

    public WorldService(MapManager mapManager,
                        WorldRepositoryInMemory worldRepositoryInMemory,
                        WorldRepositoryPersistent worldRepositoryPersistent,
                        ContinentalDriftManager continentalDriftManager,
                        WorldCleanser worldCleanser){
        this.worldRepositoryInMemory = worldRepositoryInMemory;
        this.mapManager = mapManager;
        this.worldRepositoryPersistent = worldRepositoryPersistent;
        this.continentalDriftManager = continentalDriftManager;
        this.worldCleanser = worldCleanser;
    }

    @SuppressWarnings("unused")
    @Transactional("persistentDatabaseTransactionManager")
    public void saveAndShutdownAll(){
        activeWorldIds.stream().map(mapManager::getUninitiatedWorld).forEach(worldRepositoryPersistent::save);
        activeWorldIds.clear();
    }

    @SuppressWarnings("unused")
    public void saveAndShutdown(Long id){
        saveWorld(id);
        shutdownWorld(id);
    }

    public void shutdownWorld(Long id){
        activeWorldIds.stream()
                .filter(worldId -> worldId.equals(id))
                .findFirst()
                .ifPresent(activeWorldIds::remove);
    }

    public void saveWorlds(){
        activeWorldIds.forEach(
                this::saveWorld
        );
    }

    @Transactional("persistentDatabaseTransactionManager")
    public void saveWorld(Long id){
        //World world = mapManager.getWorld(id, false);
        //worldRepositoryPersistent.save(world);
    }

    public void processTurns(){
        activeWorldIds.stream().map(mapManager::getInitiatedWorld).forEach(this::processTurn);
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    private void processTurn(World world){
        log.debug("There is currently " + world.getCoordinates().stream().map(Coordinate::getTile)
                        .map(Tile::getBiome)
                        .map(Biome::getCurrentFood)
                        .reduce(0f, (tile1, tile2) -> tile1 + tile2)
        + "food in the world"
        );
        log.debug("The total fertility in the world amounts to " + world.getCoordinates().parallelStream().map(Coordinate::getTile)
                .map(Tile::getBiome)
                .map(Biome::getFertility)
                .reduce(0f, (tile1, tile2) -> tile1 + tile2, (tile1, tile2) -> tile1 + tile2)
                + "food");
        world.getCoordinates().parallelStream().map(Coordinate::getTile).forEach(
                worldTile -> worldTile.getBiome().processParallelTask()
        );

        log.debug(world.getActorList().size() + " Actors at the start of this turn");
        log.debug(world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .count() + " Actors where dead at the start of this turn");

        world.getActorList().forEach(Actor::processSerialTask);

        worldCleanser.proces(world);
        continentalDriftManager.process(world);
        worldRepositoryInMemory.save(world);
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

    private Boolean addActiveWorldFromPersistence(Long id) {
        if(worldRepositoryPersistent.existsById(id)) {
            World world = worldRepositoryPersistent.getOne(id);
            worldRepositoryInMemory.save(world);
            if (!activeWorldIds.contains(id)) {
                activeWorldIds.add(id);
                return true;
            } else {
                return null;
            }
        } else {
            return false;
        }
    }

    private Boolean addActiveWorldFromMemory(Long id) {
        if (worldRepositoryInMemory.existsById(id)) {
            if (!activeWorldIds.contains(id)) {
                activeWorldIds.add(id);
                return true;
            } else {
                return null;
            }
        } else {
            mapManager.getWorld(id, false);
            activeWorldIds.add(id);
            return true;
        }
    }
}
