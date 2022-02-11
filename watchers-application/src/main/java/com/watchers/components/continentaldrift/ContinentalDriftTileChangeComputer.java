package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class ContinentalDriftTileChangeComputer {

    private WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("World was lost in memory."));
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        CoordinateHelper.getAllPossibleCoordinates(world)
                .forEach( coordinate -> {
                    List<Tile> tiles = newTileLayout.get(coordinate);
                    if(tiles == null){
                        throw new RuntimeException("A part of the changesmap was not set");
                    } else if ( tiles.size() == 0) {
                        processAbsentTile(coordinate, changes);
                    } else if (tiles.size() == 1) {
                        processOneTile(coordinate, tiles.get(0), changes);
                    } else {
                        processMultipleTiles(coordinate, tiles, taskDto, world);
                    }
                }
        );

        taskDto.setChanges(changes);
        worldRepository.save(world);
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

    private void processMultipleTiles(Coordinate coordinate, List<Tile> tiles, ContinentalDriftTaskDto taskDto, World world) {
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        ContinentalChangesDto dto = new ContinentalChangesDto(coordinate);
        changes.put(coordinate, dto);

        Tile survivingTile = RandomHelper.getRandomHighestTile(tiles);
        Direction survivingDirection = survivingTile.getCoordinate().getContinent().getDirection();
        MockTile mockTile = new MockTile(survivingTile);
        dto.setMockTile(mockTile);
        tiles.remove(survivingTile);


        for (Tile tile : tiles) {
            Direction direction = tile.getCoordinate().getContinent().getDirection();
            direction.adjustPressureFromIncomingDirection(survivingDirection);

            long addedHeight = tile.getHeight() / world.getWorldSettings().getHeigtDivider();
            long lostHeight = tile.getHeight() - addedHeight;
            taskDto.setHeightLoss(taskDto.getHeightLoss() + lostHeight);
            mockTile.setHeight(mockTile.getHeight() + addedHeight);

            tile.transferData(mockTile);
        }

        world.setHeightDeficit(world.getHeightDeficit() + taskDto.getHeightLoss());
        taskDto.setHeightLoss(0);

    }

}
