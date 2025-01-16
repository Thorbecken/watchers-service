package com.watchers.components.continentaldrift;

import com.watchers.components.ContinentalComputer;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Component
public class ErosionAdjuster implements ContinentalComputer {

    private final int NUMBER_OF_NEIGHBOURS_PLUS_ONE = 5;

    public ErosionAdjuster(
            @Value("${watch.continent.land.erosion.strength}") int erosionStrength,
            @Value("${watch.continent.land.erosion.max}") int maxErosion,
            @Value("${watch.continent.land.erosion.min}") int minHeightDifference) {
        this.erosionStrength = erosionStrength;
        this.maxErosion = maxErosion;
        this.minHeightDifference = minHeightDifference;
    }

    private final int erosionStrength;
    private final int maxErosion;
    private final int minHeightDifference;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        for (int i = 0; i < erosionStrength; i++) {
            this.erodeWorld(world);
        }
    }

    private void erodeWorld(World world) {
        Map<Coordinate, Long> erosionMap = new HashMap<>();

        CoordinateHelper.getAllPossibleCoordinates(world)
                .forEach(coordinate -> erosionMap.put(coordinate, 0L));

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

        BiConsumer<Coordinate, Long> erosionConsumer = (Coordinate coordinate, Long erosion) -> coordinate.getTile().setHeight(coordinate.getTile().getHeight() + erosion);
        erosionMap.forEach(erosionConsumer);
    }
}
