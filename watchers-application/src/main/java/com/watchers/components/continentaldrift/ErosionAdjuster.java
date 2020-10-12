package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ErosionAdjuster {

    private CoordinateHelper coordinateHelper;
    private int minHeightDifference;
    private int maxErosion;
    private WorldRepositoryInMemory worldRepositoryInMemory;

    public ErosionAdjuster(CoordinateHelper coordinateHelper, @Value("${watch.erosion.minHeightDifference}") int minHeightDifference, @Value("${watch.erosion.max}") int maxErosion, WorldRepositoryInMemory worldRepositoryInMemory) {
        this.coordinateHelper = coordinateHelper;
        this.minHeightDifference = minHeightDifference;
        this.maxErosion = maxErosion;
        this.worldRepositoryInMemory = worldRepositoryInMemory;
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepositoryInMemory.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        Map<Coordinate, Long> erosionMap = new HashMap<>();

        coordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
            erosionMap.put(coordinate, 0L);
        });

        Set<Coordinate> coordinates = world.getCoordinates();

        coordinates.stream().map(Coordinate::getTile).forEach(tile -> {
            List<Tile> neighbouringTiles = tile.getNeighbours();
            List<Tile> receivingTiles = neighbouringTiles.stream()
                    .filter(neighbouringTile -> (tile.getHeight() - neighbouringTile.getHeight()) > minHeightDifference)
                    .collect(Collectors.toList());

            for (Tile recievingTile : receivingTiles) {
                long heightTransfer = (tile.getHeight() - recievingTile.getHeight()) / 4;
                if (heightTransfer > maxErosion) {
                    heightTransfer = maxErosion;
                }

                long aLong = erosionMap.get(recievingTile.getCoordinate());
                erosionMap.put(recievingTile.getCoordinate(), aLong + heightTransfer);
                long anotherLong = erosionMap.get(tile.getCoordinate());
                erosionMap.put(tile.getCoordinate(), anotherLong - heightTransfer);
            }
        });

        erosionMap.forEach((Coordinate coordiante, Long aLong) -> {
                    long currentHeight = world.getTile(coordiante).getHeight();
                    world.getTile(coordiante).setHeight(currentHeight + aLong);
                }
        );

        worldRepositoryInMemory.save(world);
    }
}
