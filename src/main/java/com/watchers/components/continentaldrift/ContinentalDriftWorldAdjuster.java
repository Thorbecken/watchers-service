package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ContinentalDriftWorldAdjuster {

    private CoordinateHelper coordinateHelper;

    public ContinentalDriftWorldAdjuster(CoordinateHelper coordinateHelper){
        this.coordinateHelper = coordinateHelper;
    }

    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();

        long newHeight = calculateNewHeight(world, changes);

        coordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
                    ContinentalChangesDto dto = changes.get(coordinate);
                    if(dto.isEmpty()) {
                        createFreshTile(dto, newHeight, world);
                    } else {
                        ChangeTileOfCoordinate(dto, world);
                    }
                }
        );

        world.fillTransactionals();
    }

    private long calculateNewHeight(World world, Map<Coordinate, ContinentalChangesDto> changes) {
        long totalHeight = world.getHeightDeficit();
        long divider = coordinateHelper.getAllPossibleCoordinates(world).stream()
                .map(changes::get)
                .filter(ContinentalChangesDto::isEmpty)
                .count();
        long spendableHeight = divider==0?0:totalHeight/divider;
        world.setHeightDeficit(totalHeight-(spendableHeight*divider));
        return spendableHeight;
    }

    private void createFreshTile(ContinentalChangesDto dto, long newHeight, World world) {
        Continent assignedContinent = dto.getNewMockContinent().getContinent();
        Tile tile = world.getTile(dto.getKey());
        tile.clear();
        tile.getCoordinate().setContinent(assignedContinent);
        assignedContinent.getCoordinates().add(dto.getKey());
        tile.setHeight(newHeight);

        world.getContinents().add(assignedContinent);
    }

    private void ChangeTileOfCoordinate(ContinentalChangesDto dto, World world) {
        Tile tile = world.getTile(dto.getKey());
        tile.clear();
        tile.setData(dto.getMockTile());
    }
}
