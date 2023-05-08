package com.watchers.components.continentaldrift;

import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockCoordinate;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ContinentalDriftTileChangeComputer {

    private WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();

        taskDto.getNewTileLayout().keySet().forEach(mockCoordinate -> {
                    Coordinate coordinate = world.getCoordinate(mockCoordinate);
                    List<Tile> tiles = taskDto.getCoordinateChangeList(mockCoordinate).stream()
                            .map(MockTile::getMockCoordinateOfOrigin)
                            .map(world::getCoordinate)
                            .map(Coordinate::getTile)
                            .collect(Collectors.toList());
                    if (tiles.size() == 0) {
                        processAbsentTile(coordinate, taskDto);
                    } else if (tiles.size() == 1) {
                        processOneTile(coordinate, tiles.get(0), taskDto);
                    } else {
                        processMultipleTiles(coordinate, tiles, taskDto, world);
                    }
                }
        );
    }

    private void processAbsentTile(Coordinate coordinate, ContinentalDriftTaskDto taskDto) {
        ContinentalChangesDto dto = new ContinentalChangesDto(new MockCoordinate(coordinate));
        dto.setEmpty(true);
        taskDto.addChange(new MockCoordinate(coordinate), dto);
    }

    private void processOneTile(Coordinate coordinate, Tile tile, ContinentalDriftTaskDto taskDto) {
        ContinentalChangesDto dto = new ContinentalChangesDto(new MockCoordinate(coordinate));
        dto.setMockTile(new MockTile(tile));
        taskDto.addChange(new MockCoordinate(coordinate), dto);
    }

    private void processMultipleTiles(Coordinate coordinate, List<Tile> tiles, ContinentalDriftTaskDto taskDto, World world) {
        ContinentalChangesDto dto = new ContinentalChangesDto(new MockCoordinate(coordinate));
        taskDto.addChange(new MockCoordinate(coordinate), dto);

        Tile survivingTile = RandomHelper.getRandomHighestTile(tiles);
        Coordinate survivingCoordinate = survivingTile.getCoordinate();
        Direction survivingDirection = survivingCoordinate.getContinent().getDirection();
        MockTile mockTile = new MockTile(survivingTile);

        dto.setMockTile(mockTile);
        tiles.remove(survivingTile);


        for (Tile toBeRemovedTile : tiles) {
            Direction direction = toBeRemovedTile.getCoordinate().getContinent().getDirection();
            direction.adjustPressureFromIncomingDirection(survivingDirection);

            long addedHeight = toBeRemovedTile.getHeight() / world.getWorldSettings().getHeigtDivider();
            long lostHeight = toBeRemovedTile.getHeight() - addedHeight;
            taskDto.setHeightLoss(taskDto.getHeightLoss() + lostHeight);
            mockTile.setHeight(mockTile.getHeight() + addedHeight);

            toBeRemovedTile.transferData(mockTile, survivingCoordinate);
        }

        survivingTile.checkIntegrity();
        survivingTile.getNeighbours().forEach(Tile::checkIntegrity);

        world.setHeightDeficit(world.getHeightDeficit() + taskDto.getHeightLoss());
        taskDto.setHeightLoss(0);

    }

}
