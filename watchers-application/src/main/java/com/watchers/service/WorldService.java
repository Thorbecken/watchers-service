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
        if (!exists){
            log.warn("World " + memoryWorld.getId() + " was not found in the persistent database!");
            World persistentWorld = new World(memoryWorld.getXSize(), memoryWorld.getYSize());
            persistentWorld.setId(memoryWorld.getId());
            worldRepositoryPersistent.save(persistentWorld);

            persistentWorld.basicCopy(memoryWorld);
            worldRepositoryPersistent.save(persistentWorld);

            persistentWorld.coordinateCopy(memoryWorld);
            worldRepositoryPersistent.save(persistentWorld);
            log.warn("The missing world is now saved to persistence.");
        } else {
            log.info("World is beeing updated from memory.");
            worldRepositoryPersistent.save(memoryWorld);
        }
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

    @Transactional("persistentDatabaseTransactionManager")
    private Boolean addActiveWorldFromPersistence(Long id) {
        Optional<World> optionalWorld = worldRepositoryPersistent.findById(id);
        if(optionalWorld.isPresent()) {
            World world = optionalWorld.get();
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

    @Transactional("inmemoryDatabaseTransactionManager")
    private void saveToMemory(World world) {
        worldRepositoryInMemory.save(world);
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
