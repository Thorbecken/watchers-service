package com.watchers.components.cleaners;

import com.watchers.model.actors.Actor;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.StateType;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AnimalCleaner {

    @Transactional
    public void process(ContinentalDriftTaskDto continentalDriftTaskDto) {
        World world = continentalDriftTaskDto.getWorld();

        world.getActorList().stream()
                .filter(Actor::isNotOnCorrectLand)
                .forEach(Actor::handleContinentalMovement);

        List<Actor> deadActorsByContinentalDrifting = world.getActorList().stream()
                .filter(actor -> !StateType.DEAD.equals(actor.getStateType()))
                .filter(Actor::isNotOnCorrectLand)
                .collect(Collectors.toList());
        log.debug(deadActorsByContinentalDrifting.size() + " Actors died because of continental movement");

        deadActorsByContinentalDrifting.forEach(deadActor -> {
            if (deadActor.getCoordinate() != null) {
                deadActor.getCoordinate().getActors().remove(deadActor);
            }
            deadActor.setCoordinate(null);
            deadActor.setStateType(StateType.DEAD);
        });

    }
}
