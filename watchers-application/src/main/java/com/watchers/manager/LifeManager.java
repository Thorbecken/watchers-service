package com.watchers.manager;

import com.watchers.components.life.ActorProcessor;
import com.watchers.components.life.BiomeProcessor;
import com.watchers.model.actor.AnimalType;
import com.watchers.model.actor.animals.AnimalFactory;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Service
@AllArgsConstructor
public class LifeManager {

    WorldRepository worldRepository;
    BiomeProcessor biomeProcessor;
    ActorProcessor actorProcessor;

    public void process(WorldTaskDto taskDto){
        biomeProcessor.process(taskDto);
        actorProcessor.process(taskDto);
    }

    @Transactional
    public void seedLife(Long worldId, Long xCoord, Long yCoord) {
        World world = worldRepository.findById(worldId).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Tile seedingTile = world.getCoordinate(xCoord, yCoord).getTile();
        AnimalType animalType = selectAnimalSeed(seedingTile.getSurfaceType());
        seedingTile.getCoordinate().getActors().add(AnimalFactory.generateNewAnimal(animalType, seedingTile.getCoordinate()));
        worldRepository.save(world);
        Assert.isTrue(world.getCoordinates().size() == world.getXSize()*world.getYSize(), "coordinates were " +world.getCoordinates().size());
    }

    public static void seedLife(Coordinate coordinate) {
        AnimalType animalType = selectAnimalSeed(coordinate.getTile().getSurfaceType());
        coordinate.getActors().add(AnimalFactory.generateNewAnimal(animalType, coordinate));
    }

    private static AnimalType selectAnimalSeed(SurfaceType type) {
        switch (type){
            case PLAIN: return AnimalType.RABBIT;
            case COASTAL:
            case OCEANIC:
            case DEEP_OCEAN:
                return AnimalType.WHALE;
            default: return AnimalType.RABBIT;
        }
    }
}
