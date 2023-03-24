package com.watchers.components.life;

import com.watchers.model.actors.Actor;
import com.watchers.model.actors.Animal;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.AnimalType;
import com.watchers.model.enums.StateType;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ActorProcessor {

    private WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto){
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(()-> new RuntimeException("The world was lost in memory."));

        log.debug(world.getActorList().size() + " Actors at the start of this turn");
        log.debug(world.getActorList().stream()
                .filter(actor -> actor.getStateType() == StateType.DEAD)
                .count() + " Actors where dead at the start of this turn");

        List<Actor> currentActors = world.getActorList();
        Assert.notNull(currentActors, "There was no list found of Actors!");
        if (currentActors.size() == 0){
            log.info("The actorslist was empty!");
            world.getCoordinates().stream()
                    .map(Coordinate::getTile)
                    .filter(Tile::isLand)
                    .map(Tile::getBiome)
                    .filter(biome -> biome.getCurrentFood() > 1)
                    .findFirst()
                    .map(Biome::getTile)
                    .map(Tile::getCoordinate)
                    .ifPresent(coordinate -> coordinate.getActors().add(new Animal(coordinate, AnimalType.RABBIT, AnimalType.RABBIT.getMaxFoodReserve())));
        } else {
            currentActors.forEach(Actor::processSerialTask);
        }

        worldRepository.save(world);
    }
}
