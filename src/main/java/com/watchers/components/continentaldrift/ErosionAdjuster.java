package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ErosionAdjuster {

    private CoordinateHelper coordinateHelper;

    public ErosionAdjuster(CoordinateHelper coordinateHelper){
        this.coordinateHelper = coordinateHelper;
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

            if(tile.getHeight()-lowestNeighbour.getHeight() >= 12){
                long aLong = erosionMap.get(lowestNeighbour.getCoordinate());
                erosionMap.put(lowestNeighbour.getCoordinate(), aLong+3);
                long anotherLong = erosionMap.get(tile.getCoordinate());
                erosionMap.put(tile.getCoordinate(), anotherLong-3);
            } else if(tile.getHeight()-lowestNeighbour.getHeight() >= 8){
                long aLong = erosionMap.get(lowestNeighbour.getCoordinate());
                erosionMap.put(lowestNeighbour.getCoordinate(), aLong+2);
                long anotherLong = erosionMap.get(tile.getCoordinate());
                erosionMap.put(tile.getCoordinate(), anotherLong-2);
            } else if(tile.getHeight()-lowestNeighbour.getHeight() >= 4){
                long aLong = erosionMap.get(lowestNeighbour.getCoordinate());
                erosionMap.put(lowestNeighbour.getCoordinate(), aLong+1);
                long anotherLong = erosionMap.get(tile.getCoordinate());
                erosionMap.put(tile.getCoordinate(), anotherLong-1);
            }
        });

        erosionMap.forEach((Coordinate coordiante, Long aLong) -> {
                    long currentHeight = world.getTile(coordiante).getHeight();
                    world.getTile(coordiante).setHeight(currentHeight + aLong);
                }
        );
    }
}
