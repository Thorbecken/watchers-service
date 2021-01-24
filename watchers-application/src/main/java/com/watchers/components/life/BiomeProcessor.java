package com.watchers.components.life;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Biome;
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
public class BiomeProcessor {

    private WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto){
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in time."));
        log.debug("There is currently " + world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .map(Biome::getCurrentFood)
                .reduce(0f, (tile1, tile2) -> tile1 + tile2)
                + "food in the world"
        );
        log.debug("The total fertility in the world amounts to " + world.getCoordinates().parallelStream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .map(Biome::getFertility)
                .reduce(0f,
                        (tile1, tile2) -> tile1 + tile2,
                        (tile1, tile2) -> tile1 + tile2)
                + "food");
        world.getCoordinates().parallelStream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .forEach(Biome::processParallelTask);

        worldRepository.save(world);
    }
}
