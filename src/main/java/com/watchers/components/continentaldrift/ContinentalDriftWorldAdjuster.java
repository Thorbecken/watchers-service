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
        world.setTiles(new HashSet<>());

        coordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
                    ContinentalChangesDto dto = changes.get(coordinate);
                    if(dto.isEmpty()) {
                        createNewTile(dto, newHeight, world);
                    } else {
                        ChangeCoordinate(dto, world);
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

    private void createNewTile(ContinentalChangesDto dto, long newHeight, World world) {
        Continent assignedContinent = dto.getNewMockContinent().getContinent();
        Tile newTile = new Tile(dto.getKey(), world, assignedContinent);
        assignedContinent.getTiles().add(newTile);

        newTile.setHeight(newHeight);
        world.getTiles().add(newTile);

        world.getContinents().add(assignedContinent);
    }

    private void ChangeCoordinate(ContinentalChangesDto dto, World world) {
        Tile newTile = dto.getNewTile();
        newTile.setCoordinate(dto.getKey());
        world.getTiles().add(newTile);
    }
}
