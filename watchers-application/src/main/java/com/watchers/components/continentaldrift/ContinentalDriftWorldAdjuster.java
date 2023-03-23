package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.*;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@AllArgsConstructor
public class ContinentalDriftWorldAdjuster {

    private final WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));

        long newHeight = calculateNewHeight(world, taskDto.getChanges());

        CoordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
                    ContinentalChangesDto dto = taskDto.getChange(coordinate);
                    if (dto.isEmpty()) {
                        createFreshTile(dto, newHeight, world);
                    } else {
                        ChangeTileOfCoordinate(dto, world);
                    }
                }
        );

        worldRepository.saveAndFlush(world);
    }

    private long calculateNewHeight(World world, Map<MockCoordinate, ContinentalChangesDto> changes) {
        long heightDeficit = world.getHeightDeficit();
        long divider = getWeightedDivider(changes, world.getWorldSettings());
        long spendableHeightPerWeight = divider == 0 ? 0 : heightDeficit / divider;
        long usedHeight = spendableHeightPerWeight * divider;
        world.setHeightDeficit(heightDeficit - usedHeight);
        return spendableHeightPerWeight;
    }

    private long getWeightedDivider(Map<MockCoordinate, ContinentalChangesDto> changes, WorldSettings worldSettings) {
        long numberOfOceanicCoordinates = getContinentTypeCount(changes, SurfaceType.OCEAN);
        long numberOfContinentalCoordinates = getContinentTypeCount(changes, SurfaceType.PLAIN) * worldSettings.getContinentalContinentWeight();
        return numberOfOceanicCoordinates + numberOfContinentalCoordinates;
    }

    private long getContinentTypeCount(Map<MockCoordinate, ContinentalChangesDto> changes, SurfaceType surfaceType) {
        return changes.values().stream()
                .filter(ContinentalChangesDto::isEmpty)
                .map(ContinentalChangesDto::getMockContinentDto)
                .map(MockContinentDto::getSurfaceType)
                .filter(surfaceType::equals)
                .count();
    }

    private void createFreshTile(ContinentalChangesDto dto, long newHeight, World world) {
        MockContinentDto mockContinentDto = dto.getMockContinentDto();
        Continent assignedContinent;
        if (mockContinentDto.getSurfaceType() == null) {
            assignedContinent = world.getContinentFromId(mockContinentDto.getContinentId());
        } else {
            assignedContinent = new Continent(world, mockContinentDto.getSurfaceType());
            assignedContinent.setId(mockContinentDto.getContinentId());
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
        Tile tile = world.getCoordinate(dto.getKey()).getTile();
        tile.clear();

        Coordinate deletedTileCoordinate = world.getCoordinate(mockTile.getMockCoordinateOfOrigin());
        tile.setData(mockTile, deletedTileCoordinate);
    }
}
