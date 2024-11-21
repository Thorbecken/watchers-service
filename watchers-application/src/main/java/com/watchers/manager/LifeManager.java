package com.watchers.manager;

import com.watchers.components.life.ActorProcessor;
import com.watchers.components.life.BiomeProcessor;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.actors.Animal;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.enums.AnimalType;
import com.watchers.model.enums.FloraTypeEnum;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Flora;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class LifeManager {

    private final WorldRepository worldRepository;
    private final BiomeProcessor biomeProcessor;
    private final ActorProcessor actorProcessor;

    public void process(WorldTaskDto taskDto) {
        StopwatchTimer.start();
        biomeProcessor.process(taskDto);
        StopwatchTimer.stop("BiomeProcessor");
        StopwatchTimer.start();
        actorProcessor.process(taskDto);
        StopwatchTimer.stop("ActorProcessor");
    }

    @Transactional
    public void seedLife(Long xCoord, Long yCoord, AnimalType animalType) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Tile seedingTile = world.getCoordinate(xCoord, yCoord).getTile();
        seedingTile.getCoordinate().getActors().add(new Animal(seedingTile.getCoordinate(), animalType, animalType.getMaxFoodReserve()));
        worldRepository.save(world);
    }

    @Transactional
    public void seedFlora(Long xCoord, Long yCoord, Flora flora) {
        World world = worldRepository.findById(1L).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Coordinate seedingCoordinate = world.getCoordinate(xCoord, yCoord);
        if(FloraTypeEnum.TREE.equals(flora.getType())){
            seedingCoordinate.getTile().getBiome().setTreeFlora(flora);
        } else {
            seedingCoordinate.getTile().getBiome().setGrassFlora(flora);
        }

        worldRepository.save(world);
    }

    public static void seedLife(Coordinate coordinate) {
        AnimalType animalType = selectAnimalSeed(coordinate.getTile().getSurfaceType());
        coordinate.getActors().add(new Animal(coordinate, animalType, animalType.getMaxFoodReserve()));
    }

    private static AnimalType selectAnimalSeed(SurfaceType type) {
        switch (type) {
            case MOUNTAIN:
            case HILL:
            case PLAIN:
                return AnimalType.RABBIT;
            case COASTAL:
            case SEA:
            case OCEAN:
                return AnimalType.WHALE;
            default:
                return AnimalType.RABBIT;
        }
    }
}
