package com.watchers.components.cleaners;

import com.watchers.model.actors.Actor;
import com.watchers.model.enums.StateType;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.ContinentRepository;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class WorldCleaner {

    private WorldRepository worldRepository;
    private ContinentRepository continentRepository;

    @Transactional
    public void proces(WorldTaskDto dto){
        World world = worldRepository.findById(dto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
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

        if(dto instanceof ContinentalDriftTaskDto) {
            List<Continent> zeroContinents = world.getContinents().stream().filter(continent -> continent.getCoordinates().size() == 0).collect(Collectors.toList());
            if (zeroContinents.size() > 0) {
                int start = world.getContinents().size();
                log.trace("deleting continents: " + Arrays.toString(zeroContinents.stream().map(Continent::getId).toArray()));
                world.getContinents().removeAll(zeroContinents);

                worldRepository.saveAndFlush(world);

                world = worldRepository.findById(world.getId()).orElseThrow(() -> new RuntimeException("World was lost in memory"));
                continentRepository.deleteAll(zeroContinents);
                zeroContinents.stream().map(Continent::getId).forEach(aLong -> ((ContinentalDriftTaskDto) dto).getRemovedContinents.add(aLong));
                log.trace("Started with " + start + " continents and ended with " + world.getContinents().size());
            }
        }

        worldRepository.saveAndFlush(world);
    }
}
