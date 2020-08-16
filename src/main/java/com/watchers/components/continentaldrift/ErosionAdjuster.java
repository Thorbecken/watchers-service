package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ErosionAdjuster {

    private CoordinateHelper coordinateHelper;
    private int maxErosion;

    public ErosionAdjuster(CoordinateHelper coordinateHelper, @Value("${watch.maxErosion}") int maxErosion){
        this.coordinateHelper = coordinateHelper;
        this.maxErosion = maxErosion;
    }

    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Map<Coordinate, Long> erosionMap = new HashMap<>();

        coordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
            erosionMap.put(coordinate, 0L);
        });

        Set<Tile> tiles = taskDto.getWorld().getTiles();

        tiles.forEach(tile -> {
            List<Tile> neighbouringTiles = tile.getNeighbours();
            Tile lowestNeighbour = neighbouringTiles.stream()
                    .min(Comparator.comparing(Tile::getHeight))
                    .get();

            long heightTransfer = (tile.getHeight()-lowestNeighbour.getHeight()) / 4;
            if(heightTransfer > maxErosion){
                heightTransfer = maxErosion;
            }
            if(heightTransfer > 0){
                long aLong = erosionMap.get(lowestNeighbour.getCoordinate());
                erosionMap.put(lowestNeighbour.getCoordinate(), aLong+heightTransfer);
                long anotherLong = erosionMap.get(tile.getCoordinate());
                erosionMap.put(tile.getCoordinate(), anotherLong-heightTransfer);
            }
        });

        erosionMap.forEach((Coordinate coordiante, Long aLong) -> {
                    long currentHeight = world.getTile(coordiante).getHeight();
                    world.getTile(coordiante).setHeight(currentHeight + aLong);
                }
        );
    }
}
