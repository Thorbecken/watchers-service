package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ContinentalDriftTileChangeComputer {

    private CoordinateHelper coordinateHelper;

    public ContinentalDriftTileChangeComputer(CoordinateHelper coordinateHelper){
        this.coordinateHelper = coordinateHelper;
    }

    public void process(ContinentalDriftTaskDto taskDto) {
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        coordinateHelper.getAllPossibleCoordinates(taskDto.getWorld())
                .forEach( coordinate -> {
                    List<Tile> tiles = newTileLayout.get(coordinate);
                    if(tiles == null){
                        throw new RuntimeException("A part of the changesmap was not set");
                    } else if ( tiles.size() == 0) {
                        processAbsentTile(coordinate, changes);
                    } else if (tiles.size() == 1) {
                        processOneTile(coordinate, tiles.get(0), changes);
                    } else {
                        processMultipleTiles(coordinate, tiles, taskDto);
                    }
                }
        );

        taskDto.setChanges(changes);

        taskDto.getWorld().fillTransactionals();
    }

    private void processAbsentTile(Coordinate coordinate, Map<Coordinate, ContinentalChangesDto> changes) {
        ContinentalChangesDto dto = new ContinentalChangesDto(coordinate);
        dto.setEmpty(true);
        changes.put(coordinate, dto);
    }

    private void processOneTile(Coordinate coordinate, Tile tile, Map<Coordinate, ContinentalChangesDto> changes) {
        ContinentalChangesDto dto = new ContinentalChangesDto(coordinate);
        MockTile mockTile = new MockTile(tile);
        dto.setMockTile(mockTile);
        changes.put(coordinate, dto);
    }

    private void processMultipleTiles(Coordinate coordinate, List<Tile> tiles, ContinentalDriftTaskDto taskDto) {
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        ContinentalChangesDto dto = new ContinentalChangesDto(coordinate);
        changes.put(coordinate, dto);

        Tile survivingTile = RandomHelper.getRandomHighestTile(tiles);
        MockTile mockTile = new MockTile(survivingTile);
        dto.setMockTile(mockTile);
        tiles.remove(survivingTile);


        for (Tile tile : tiles) {
            long addedHeight = tile.getHeight() / taskDto.getHeightDivider();
            taskDto.setHeightLoss(taskDto.getHeightLoss() + tile.getHeight() - addedHeight);
            mockTile.setHeight(mockTile.getHeight() + addedHeight);

            tile.transferData(mockTile);
        }

        taskDto.getWorld().setHeightDeficit(taskDto.getWorld().getHeightDeficit() + taskDto.getHeightLoss());
        taskDto.setHeightLoss(0);

    }

}