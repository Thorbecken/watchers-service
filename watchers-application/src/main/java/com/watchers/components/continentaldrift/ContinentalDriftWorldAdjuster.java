package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockContinentDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class ContinentalDriftWorldAdjuster {

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        long newHeight = 0;

        CoordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
                    ContinentalChangesDto dto = taskDto.getChange(coordinate);
                    if (dto.isEmpty()) {
                        createFreshTile(dto, newHeight, world);
                    } else {
                        ChangeTileOfCoordinate(dto, world);
                    }
                }
        );
    }

    private void createFreshTile(ContinentalChangesDto dto, long newHeight, World world) {
        MockContinentDto mockContinentDto = dto.getMockContinentDto();
        Continent assignedContinent;
        if (mockContinentDto.getContinentId() != null) {
            assignedContinent = world.getContinentFromId(mockContinentDto.getContinentId());
        } else {
            assignedContinent = new Continent(world, mockContinentDto.getSurfaceType());
        }
        Coordinate coordinate = world.getCoordinate(dto.getKey().getXCoord(), dto.getKey().getYCoord());
        Tile tile = coordinate.getTile();
        tile.clear();
        tile.getCoordinate().changeContinent(assignedContinent);
        assignedContinent.getCoordinates().add(coordinate);
        long weightedHeight = getWeightedHeight(newHeight, assignedContinent, world.getWorldSettings());
        tile.setHeight(weightedHeight);

        world.getContinents().add(assignedContinent);
    }

    private long getWeightedHeight(long newHeight, Continent assignedContinent, WorldSettings worldSettings) {
        return assignedContinent.getType().equals(SurfaceType.PLAIN) ? newHeight * worldSettings.getContinentalContinentWeight() : newHeight;
    }

    private void ChangeTileOfCoordinate(ContinentalChangesDto dto, World world) {
        MockTile mockTile = dto.getMockTile();
        Tile toBeFilledTile = world.getCoordinate(dto.getKey()).getTile();
        toBeFilledTile.clear();

        Coordinate coordinateOfOrigin = world.getCoordinate(mockTile.getCoordinateOfOrigin());
        toBeFilledTile.setData(mockTile, coordinateOfOrigin);
    }
}
