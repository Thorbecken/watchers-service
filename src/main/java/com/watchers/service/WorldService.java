package com.watchers.service;

import com.watchers.manager.MapManager;
import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Slf4j
@Service
public class WorldService {

    @Autowired
    private WorldRepository worldRepository;

    @Autowired
    private MapManager mapManager;

    private List<World> activeWorlds;

    public WorldService(){
        this.activeWorlds = new ArrayList<>();
    }

    @PostConstruct
    private void init(){
        activeWorlds.add(mapManager.getWorld(1L));
    }

    public void startWorld(Long id){
        Optional<World> optionalWorld = activeWorlds.stream()
                .filter(world -> world.getId().equals(id))
                .findFirst();
        if(!optionalWorld.isPresent()){
            activeWorlds.add(mapManager.getWorld(1L));
        }
    }

    public void saveAndShutdownAll(){
        activeWorlds.forEach(worldRepository::save);
        activeWorlds.clear();
    }

    public void saveAndShutdown(Long id){
        saveWorld(id);
        shutdownWorld(id);
    }

    public void shutdownWorld(Long id){
        activeWorlds.stream()
                .filter(world -> world.getId().equals(id))
                .findFirst()
                .ifPresent(activeWorlds::remove);
    }

    public void saveWorlds(){
        getActiveWorlds().stream().map(World::getId).forEach(
                this::saveWorld
        );
    }

    public void saveWorld(Long id){
        activeWorlds.stream()
                .filter(world -> world.getId().equals(id))
                .findFirst()
                .ifPresent(worldRepository::save);
    }

    public void processTurns(){
        activeWorlds.forEach(this::processTurn);
    }

    private void processTurn(World world){
        world.getConcurrentTiles().parallelStream().forEach(
                worldTile -> worldTile.getBiome().processParallelTask()
        );

        world.getConcurrentTiles().forEach(
                worldTile -> {
                    worldTile.getConcurrentActors().forEach(Actor::processSerialTask);
                    worldTile.getConcurrentActors().removeIf(actor -> actor.getStateType() == StateType.DEAD);
                });
    }


    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void executeTurn() {
        //processTurns();

        World world = mapManager.getWorld(1L);
        processTurn(world);
        worldRepository.save(world);
        log.info("Processed a turn");
    }

}
