package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContinentalDriftWorldAdjuster {

    private CoordinateHelper coordinateHelper;

    public ContinentalDriftWorldAdjuster(CoordinateHelper coordinateHelper){
        this.coordinateHelper = coordinateHelper;
    }

    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Map<Coordinate, ContinentalChangesDto> changes = taskDto.getChanges();
        long heightLoss = taskDto.getHeightLoss();

        long newHeight = calculateNewHeight(world, heightLoss, changes);
        world.setTiles(new HashSet<>());

        coordinateHelper.getAllPossibleCoordinates(world).forEach(coordinate -> {
                    ContinentalChangesDto dto = changes.get(coordinate);
                    if(dto.isEmpty()) {
                        createNewTile(coordinate, newHeight, world);
                    } else {
                        ChangeCoordinate(coordinate, dto, world);
                    }
                }
        );

        world.fillTransactionals();
    }

    private long calculateNewHeight(World world, long heightLoss, Map<Coordinate, ContinentalChangesDto> changes) {
        long totalHeight = world.getHeightDeficit() + heightLoss;
        long divider = coordinateHelper.getAllPossibleCoordinates(world).stream()
                .map(changes::get)
                .filter(ContinentalChangesDto::isEmpty)
                .count();
        long spendableHeight = totalHeight/divider;
        world.setHeightDeficit(totalHeight-(spendableHeight*divider));
        return spendableHeight;
    }

    private void createNewTile(Coordinate coordinate, long newHeight, World world) {
        List<Coordinate> coordinates = coordinate
                .getCoordinatesWithinRange(1);

        List<Tile> tiles = coordinates
                .stream()
                .map(world::getTile)
                .filter(Objects::nonNull)
                .filter(tile -> tile.getCoordinate() != null)
                .collect(Collectors.toList());
        List<Continent> continents = tiles.stream().map(Tile::getContinent).collect(Collectors.toList());

        Optional<Continent> assignedContinent = continents.stream()
                .max(Comparator.comparing(Continent::getId));
        Tile newTile = new Tile(coordinate, world, assignedContinent
                .orElseGet(() -> new Continent(world, SurfaceType.OCEANIC)));

        newTile.setHeight(newHeight);
        world.getTiles().add(newTile);
    }

    private void ChangeCoordinate(Coordinate coordinate, ContinentalChangesDto dto, World world) {
        Tile newTile = dto.getNewTile();
        newTile.setCoordinate(coordinate);
        world.getTiles().add(newTile);
    }
}
