package com.watchers.service;

import com.watchers.manager.MapManager;
import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import com.watchers.repository.postgres.WorldRepositoryPersistent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@Service
@EnableTransactionManagement
public class WorldService {

    @Autowired
    private WorldRepositoryInMemory worldRepositoryInMemory;

    @Autowired
    private WorldRepositoryPersistent worldRepositoryPersistent;

    @Autowired
    private MapManager mapManager;

    @Transient
    private List<Long> activeWorldIds;

    public WorldService(){
        this.activeWorldIds = new ArrayList<>();
    }

    @PostConstruct
    private void init(){
        activeWorldIds.add(1L);
        mapManager.getWorld(1L, false);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void startWorld(Long id){
        Long activeWorldID = activeWorldIds.stream()
            .filter(world -> world.equals(id))
            .findFirst()
            .orElse(mapManager.getWorld(id, false).getId());
        if(!activeWorldIds.contains(activeWorldID)){
            activeWorldIds.add(activeWorldID);
        }
    }

    @Transactional("persistentDatabaseTransactionManager")
    public void saveAndShutdownAll(){
        activeWorldIds.stream().map(mapManager::getUninitiatedWorld).forEach(worldRepositoryPersistent::save);
        activeWorldIds.clear();
    }

    public void saveAndShutdown(Long id){
        saveWorld(id);
        shutdownWorld(id);
    }

    public void shutdownWorld(Long id){
        getActiveWorldIds().stream()
                .filter(worldId -> worldId.equals(id))
                .findFirst()
                .ifPresent(activeWorldIds::remove);
    }

    @Transactional("persistentDatabaseTransactionManager")
    public void saveWorlds(){
        getActiveWorldIds().forEach(
                this::saveWorld
        );
    }

    @Transactional("persistentDatabaseTransactionManager")
    public void saveWorld(Long id){
        worldRepositoryPersistent.save(mapManager.getWorld(id, false));
    }

    public void processTurns(){
        activeWorldIds.stream().map(mapManager::getInitiatedWorld).forEach(this::processTurn);
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    private void processTurn(World world){
        log.info("There is currently " + world.getTiles().stream()
                        .map(Tile::getBiome)
                        .map(Biome::getCurrentFood)
                        .reduce(0f, (tile1, tile2) -> tile1 + tile2)
        + "food in the world"
        );
        log.info("The total fertility in the world amounts to " + world.getTiles().parallelStream()
                .map(Tile::getBiome)
                .map(Biome::getFertility)
                .reduce(0f, (tile1, tile2) -> tile1 + tile2, (tile1, tile2) -> tile1 + tile2)
                + "food");
        world.getTiles().parallelStream().forEach(
                worldTile -> worldTile.getBiome().processParallelTask()
        );

        log.info(world.getActorList().size() + " Actors at the start of this turn");
        log.info(world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .count() + " Actors where dead at the start of this turn");

        world.getActorList().forEach(Actor::processSerialTask);

        List<Actor> currentDeads = world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .collect(Collectors.toList());
        log.info(currentDeads.size() + " Actors died this turn");
        currentDeads.forEach( deadActor -> {
            deadActor.getTile().getActors().remove(deadActor);
            deadActor.setTile(null);
        });

        log.info(world.getActorList().size() + " Actors remained before cleansing the dead this turn");

        world.getActorList().removeAll(currentDeads);

        log.info(world.getNewActors().size() + " Actors were born into this world");
        world.getActorList().addAll(world.getNewActors());
        world.getNewActors().clear();

        log.info(world.getActorList().size() + " Actors remained this turn");

        worldRepositoryInMemory.save(world);
    }


    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void executeTurn() {
        processTurns();
        log.info("Processed a turn");
    }

}
