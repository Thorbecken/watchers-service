package com.watchers.components.cleaners;

import com.watchers.model.actors.Actor;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.StateType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AnimalCleaner {

    private WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto continentalDriftTaskDto) {
        World world = worldRepository.findById(continentalDriftTaskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));

        List<Actor> deadActorsByContinentalDrifting = world.getActorList().stream()
                .filter(Actor::isNotOnCorrectLand)
                .collect(Collectors.toList());
        log.debug(deadActorsByContinentalDrifting.size() + " Actors died because of continental movement");
        deadActorsByContinentalDrifting.forEach(deadActor -> {
            if (deadActor.getCoordinate() != null) {
                deadActor.getCoordinate().getActors().remove(deadActor);
            }
            deadActor.setCoordinate(null);
        });

        world.getActorList().removeAll(deadActorsByContinentalDrifting);

        worldRepository.saveAndFlush(world);
    }

//    old code
//    TODO: fix this so that the sad animals can be saved from extinction
//    problem was detached entities since the addition of a overwriten hash en equals methode
//    {
//        World world = worldRepository.findById(continentalDriftTaskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memoroy"));
//
//        world.getActorList().stream()
//                .filter(Actor::isNotOnCorrectLand)
//                .forEach(Actor::handleContinentalMovement);
//
//        worldRepository.saveAndFlush(world);
//    }

}
