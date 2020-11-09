package com.watchers.components.continentaldrift;

import com.watchers.config.SettingConfiguration;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@AllArgsConstructor
public class ContinentalDriftWorldAdjuster {

    private CoordinateHelper coordinateHelper;
    private WorldRepository worldRepository;
    private SettingConfiguration settingConfiguration;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
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

        worldRepository.save(world);
    }

    private long calculateNewHeight(World world, Map<Coordinate, ContinentalChangesDto> changes) {
        long heightDeficit = world.getHeightDeficit();
        long divider = getWeightedDivider(changes);
        long spendableHeightPerWeight = divider == 0 ? 0:heightDeficit/divider;
        long usedHeight = spendableHeightPerWeight*divider;
        world.setHeightDeficit(heightDeficit-usedHeight);
        return spendableHeightPerWeight;
    }

    private long getWeightedDivider(Map<Coordinate, ContinentalChangesDto> changes) {
        long numberOfOceanicCoordinates = getContinentTypeCount(changes, SurfaceType.OCEANIC);
        long numberOfContinentalCoordinates = getContinentTypeCount(changes, SurfaceType.PLAIN) * settingConfiguration.getContinentalContinentWeight();
        return numberOfOceanicCoordinates + numberOfContinentalCoordinates;
    }

    private long getContinentTypeCount(Map<Coordinate, ContinentalChangesDto> changes, SurfaceType surfaceType) {
        return changes.values().stream()
                .filter(ContinentalChangesDto::isEmpty)
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent().getContinent().getType().equals(surfaceType))
                .count();
    }

    private void createFreshTile(ContinentalChangesDto dto, long newHeight, World world) {
        Continent assignedContinent = dto.getNewMockContinent().getContinent();
        Tile tile = world.getCoordinate(dto.getKey().getXCoord(), dto.getKey().getYCoord()).getTile();
        tile.clear();
        tile.getCoordinate().setContinent(assignedContinent);
        assignedContinent.getCoordinates().add(dto.getKey());
        tile.setHeight(weightedHeight(newHeight, assignedContinent));

        world.getContinents().add(assignedContinent);
    }

    private long weightedHeight(long newHeight, Continent assignedContinent) {
        return assignedContinent.getType().equals(SurfaceType.PLAIN)? newHeight * settingConfiguration.getContinentalContinentWeight() : newHeight;
    }

    private void ChangeTileOfCoordinate(ContinentalChangesDto dto, World world) {
        Tile tile = world.getCoordinate(dto.getKey().getXCoord(), dto.getKey().getYCoord()).getTile();
        tile.clear();
        tile.setData(dto.getMockTile());
    }
}
