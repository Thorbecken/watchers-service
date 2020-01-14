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
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Slf4j
@Service
public class WorldService {

    @Autowired
    private WorldRepository worldRepository;

    @Autowired
    private MapManager mapManager;

    @Transient
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

        log.info(world.getActorList().size() + " Actors at the start of this turn");
        log.info(world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .collect(Collectors.toList()).size() + " Actors where dead at the start of this turn");

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
    }


    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void executeTurn() {
        processTurns();

/*        World world = mapManager.getWorld(1L);
        processTurn(world);
        worldRepository.save(world);*/
        log.info("Processed a turn");
    }

}
