package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ContinentalDriftTileAdjuster {

    private CoordinateHelper coordinateHelper;

    public ContinentalDriftTileAdjuster(CoordinateHelper coordinateHelper){
        this.coordinateHelper = coordinateHelper;
    }

    public void process(ContinentalDriftTaskDto taskDto) {
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        coordinateHelper.getAllPossibleCoordinates(taskDto.getWorld())
                .forEach( coordinate -> {
                    List<Tile> tiles = newTileLayout.get(coordinate);
                    if (tiles == null || tiles.size() == 0) {
                        processAbsentTile(coordinate, changes);
                    } else if (tiles.size() == 1) {
                        processOneTile(coordinate, tiles.get(0), changes);
                    } else {
                        processMultipleTiles(coordinate, tiles, taskDto);
                    }
                }
        );

        taskDto.setChanges(changes);
    }

    private void processAbsentTile(Coordinate coordinate, Map<Coordinate, ContinentalChangesDto> changes) {
        ContinentalChangesDto dto = new ContinentalChangesDto(coordinate);
        dto.setEmpty(true);
        changes.put(coordinate, dto);
    }

    private void processOneTile(Coordinate coordinate, Tile tile, Map<Coordinate, ContinentalChangesDto> changes) {
        ContinentalChangesDto dto = new ContinentalChangesDto(coordinate);
        dto.setNewTile(tile);
        dto.setOldCoordinate(tile.getCoordinate());
        changes.put(coordinate, dto);
    }

    private void processMultipleTiles(Coordinate coordinate, List<Tile> tiles, ContinentalDriftTaskDto taskDto) {
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        ContinentalChangesDto dto = new ContinentalChangesDto(coordinate);
        changes.put(coordinate, dto);

        tiles.sort(Comparator.comparing(Tile::getHeight));
        Tile survivingTile = RandomHelper.getRandomHighestTile(tiles);
        dto.setNewTile(survivingTile);
        dto.setOldCoordinate(survivingTile.getCoordinate());
        tiles.remove(survivingTile);


        for (Tile tile : tiles) {
            long addedHeight = tile.getHeight() / taskDto.getHeightDivider();
            taskDto.setHeightLoss(taskDto.getHeightLoss() + tile.getHeight() - addedHeight);
            survivingTile.setHeight(survivingTile.getHeight() + addedHeight);

            taskDto.getWorld().getTiles().remove(tile);
            tile.getContinent().getTiles().remove(tile);
        }

        taskDto.getWorld().setHeightDeficit(taskDto.getWorld().getHeightDeficit() + taskDto.getHeightLoss());
        taskDto.setHeightLoss(0);
        transferActors(survivingTile, tiles);
    }

    private void transferActors(Tile newTile, List<Tile> tiles) {
        tiles.forEach( tile -> newTile.getActors().addAll(tile.getActors()));
    }

}
