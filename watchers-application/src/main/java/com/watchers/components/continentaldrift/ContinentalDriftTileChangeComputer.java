package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class ContinentalDriftTileChangeComputer {

    private WorldRepositoryInMemory worldRepositoryInMemory;
    private CoordinateHelper coordinateHelper;

    public ContinentalDriftTileChangeComputer(CoordinateHelper coordinateHelper, WorldRepositoryInMemory worldRepositoryInMemory){
        this.coordinateHelper = coordinateHelper;
        this.worldRepositoryInMemory = worldRepositoryInMemory;
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepositoryInMemory.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("World was lost in memory."));
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        coordinateHelper.getAllPossibleCoordinates(world)
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
        worldRepositoryInMemory.save(world);
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
        MockTile mockTile = new MockTile(survivingTile);
        dto.setMockTile(mockTile);
        tiles.remove(survivingTile);


        for (Tile tile : tiles) {
            long addedHeight = tile.getHeight() / taskDto.getHeightDivider();
            taskDto.setHeightLoss(taskDto.getHeightLoss() + tile.getHeight() - addedHeight);
            mockTile.setHeight(mockTile.getHeight() + addedHeight);

            tile.transferData(mockTile);
        }

        world.setHeightDeficit(world.getHeightDeficit() + taskDto.getHeightLoss());
        taskDto.setHeightLoss(0);

    }

}
