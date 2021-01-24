package com.watchers.components.continentaldrift;

import com.watchers.config.SettingConfiguration;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ErosionAdjuster {

    private final int NUMBER_OF_NEIGHBOURS = 4;
    private WorldRepository worldRepository;
    private SettingConfiguration settingConfiguration;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Map<Coordinate, Long> erosionMap = new HashMap<>();

        CoordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
            erosionMap.put(coordinate, 0L);
        });

        Set<Coordinate> coordinates = world.getCoordinates();

        coordinates.stream().map(Coordinate::getTile).forEach(tile -> {
            List<Tile> neighbouringTiles = tile.getNeighbours();
            List<Tile> receivingTiles = neighbouringTiles.stream()
                    .filter(neighbouringTile -> (tile.getHeight() - neighbouringTile.getHeight()) > settingConfiguration.getMinHeightDifference())
                    .collect(Collectors.toList());

            for (Tile recievingTile : receivingTiles) {
                long heightTransfer = (tile.getHeight() - recievingTile.getHeight()) / NUMBER_OF_NEIGHBOURS;
                if (heightTransfer > settingConfiguration.getMaxErosion()) {
                    heightTransfer = settingConfiguration.getMaxErosion();
                }

                long aLong = erosionMap.get(recievingTile.getCoordinate());
                erosionMap.put(recievingTile.getCoordinate(), aLong + heightTransfer);
                long anotherLong = erosionMap.get(tile.getCoordinate());
                erosionMap.put(tile.getCoordinate(), anotherLong - heightTransfer);
            }
        });

        erosionMap.forEach((Coordinate coordiante, Long aLong) -> {
                    long currentHeight = world.getCoordinate(coordiante.getXCoord(), coordiante.getYCoord()).getTile().getHeight();
                    world.getCoordinate(coordiante.getXCoord(), coordiante.getYCoord()).getTile().setHeight(currentHeight + aLong);
                }
        );

        worldRepository.save(world);
    }
}
