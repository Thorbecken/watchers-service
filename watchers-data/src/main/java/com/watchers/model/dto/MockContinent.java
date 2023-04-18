package com.watchers.model.dto;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class MockContinent {
    private List<Coordinate> coordinates = new ArrayList<>();
    private List<Coordinate> possibleCoordinates = new ArrayList<>();
    private Continent continent;

    public MockContinent(Continent continent) {
        this.continent = continent;

        this.coordinates.addAll(continent.getCoordinates());
        this.coordinates.forEach(coordinate -> this.possibleCoordinates.addAll(coordinate.getNeighbours())
        );
    }

    public void addRandomCoordinate(WorldFactoryDTO dto) {
        if (possibleCoordinates.isEmpty()) {
            return;
        }
        List<Coordinate> openTiles = dto.getOpenCoordinates();
        List<Coordinate> takenTiles = dto.getTakenCoordinates();

        int getInt = new Random().nextInt(possibleCoordinates.size());
        Coordinate newCoordinate = possibleCoordinates.get(getInt);
        Optional<Coordinate> openCoordinate = findOpenTile(openTiles, newCoordinate);

        if (openCoordinate.isPresent()) {
            takeOpenTile(openTiles, takenTiles, newCoordinate, openCoordinate.get());
        } else {
            possibleCoordinates.remove(newCoordinate);
            this.addRandomCoordinate(dto);
        }
    }

    private Optional<Coordinate> findOpenTile(List<Coordinate> openTiles, Coordinate newTile) {
        return openTiles.stream().filter(
                coordinate -> coordinate.equals(newTile)
        ).findFirst();
    }

    private void takeOpenTile(List<Coordinate> openCoordinates, List<Coordinate> takenCoordinates, Coordinate newCoordinate, Coordinate openCoordinate) {
        newCoordinate.getTile().setRockType(continent.getBasicRockType());
        this.coordinates.add(newCoordinate);
        takenCoordinates.add(newCoordinate);
        openCoordinates.remove(openCoordinate);
        this.possibleCoordinates.addAll(newCoordinate.getNeighbours());
        this.possibleCoordinates.removeAll(this.coordinates);
        this.possibleCoordinates = this.possibleCoordinates.stream()
                .filter(coordinate -> takenCoordinates.stream().noneMatch(
                        coordinate::equals
                )).collect(Collectors.toList());
    }

    public void generateContinent(World world) {
        Assert.isTrue(this.continent != null, "continent was null");
        continent.getCoordinates().addAll(coordinates);
        world.getCoordinates().addAll(coordinates);
        Set<Coordinate> coordinates = new HashSet<>(continent.getCoordinates());
        coordinates.forEach(
                coordinate -> {
                    coordinate.changeContinent(continent);
                    coordinate.getTile().setSurfaceType(continent.getType());
                }
        );

    }
}