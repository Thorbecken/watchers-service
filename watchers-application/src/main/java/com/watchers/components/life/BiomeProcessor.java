package com.watchers.components.life;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class BiomeProcessor {

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = taskDto.getWorld();

        world.getCoordinates().parallelStream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .forEach(Biome::processParallelTask);

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .forEach(Biome::spread);
    }
}
