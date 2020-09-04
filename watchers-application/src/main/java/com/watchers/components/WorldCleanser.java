package com.watchers.components;

import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WorldCleanser {

    private WorldRepositoryInMemory worldRepositoryInMemory;

    public WorldCleanser(WorldRepositoryInMemory worldRepositoryInMemory) {
        this.worldRepositoryInMemory = worldRepositoryInMemory;
    }

    public void proces(World world){
        List<Actor> currentDeads = world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .collect(Collectors.toList());
        log.debug(currentDeads.size() + " Actors died");
        currentDeads.forEach( deadActor -> {
            if(deadActor.getCoordinate() != null){
                deadActor.getCoordinate().getActors().remove(deadActor);
            }
            deadActor.setCoordinate(null);
        });

        log.debug(world.getActorList().size() + " Actors remained before cleansing the dead");

        world.getActorList().removeAll(currentDeads);

        log.debug(world.getNewActors().size() + " Actors were born into this world");
        world.getActorList().addAll(world.getNewActors());
        world.getNewActors().clear();

        log.debug(world.getActorList().size() + " Actors remaining");

        worldRepositoryInMemory.save(world);
        world.fillTransactionals();
    }
}
