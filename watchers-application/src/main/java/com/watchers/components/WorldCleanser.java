package com.watchers.components;

import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.environment.World;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WorldCleanser {

    public void proces(World world){
        List<Actor> currentDeads = world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .collect(Collectors.toList());
        log.trace(currentDeads.size() + " Actors died");
        currentDeads.forEach( deadActor -> {
            deadActor.getCoordinate().getActors().remove(deadActor);
            deadActor.setCoordinate(null);
        });

        log.trace(world.getActorList().size() + " Actors remained before cleansing the dead");

        world.getActorList().removeAll(currentDeads);

        log.trace(world.getNewActors().size() + " Actors were born into this world");
        world.getActorList().addAll(world.getNewActors());
        world.getNewActors().clear();

        log.trace(world.getActorList().size() + " Actors remaining");

        world.fillTransactionals();
    }
}
