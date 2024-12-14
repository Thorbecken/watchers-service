package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ErosionAdjuster {

    private final int NUMBER_OF_NEIGHBOURS_PLUS_ONE = 5;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Map<Coordinate, Long> erosionMap = new HashMap<>();

        CoordinateHelper.getAllPossibleCoordinates(world)
                .forEach(coordinate -> erosionMap.put(coordinate, 0L));

        int maxErosion = world.getWorldSettings().getMaxErosion();
        int minHeightDifference = world.getWorldSettings().getMinHeightDifference();

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .forEach(tile -> {
                    List<Tile> neighbouringTiles = tile.getNeighbours();
                    List<Tile> receivingTiles = neighbouringTiles.stream()
                            .filter(neighbouringTile -> (tile.getHeight() - neighbouringTile.getHeight()) > minHeightDifference)
                            .collect(Collectors.toList());

                    for (Tile recievingTile : receivingTiles) {
                        long heightTransfer = (tile.getHeight() - recievingTile.getHeight()) / NUMBER_OF_NEIGHBOURS_PLUS_ONE;
                        heightTransfer = Math.min(heightTransfer, maxErosion);
                        heightTransfer = Math.max(0, heightTransfer);
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
    }
}
