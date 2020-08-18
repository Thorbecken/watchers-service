package com.watchers.model.environment;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class MockContinent {
    private World world;
    private List<Coordinate> coordinates = new ArrayList<>();
    private List<Coordinate> possibleCoordinates = new ArrayList<>();
    private Continent continent;

    public MockContinent(Continent continent, World world){
        this.continent = continent;
        this.world = world;

        this.coordinates.addAll(continent.getCoordinates());
        this.coordinates.forEach(coordinate -> this.possibleCoordinates.addAll(coordinate.getNeighbours())
        );
    }

    public MockContinent(List<Coordinate> coordinates, World world){
        this.coordinates = new ArrayList<>();
        this.possibleCoordinates = new ArrayList<>();
        this.world = world;

        this.coordinates.addAll(coordinates);

        this.possibleCoordinates = CoordinateHelper.getAllOutersideCoordinates(this.coordinates);
       }

    public void addRandomCoordinate(WorldFactoryDTO dto) {
        if(possibleCoordinates.isEmpty()){
            return;
        }
        List<Coordinate> openTiles = dto.getOpenCoordinates();
        List<Coordinate> takenTiles = dto.getTakenCoordinates();

        int getInt = new Random().nextInt(possibleCoordinates.size());
        Coordinate newCoordinate = possibleCoordinates.get(getInt);
        Optional<Coordinate> openCoordinate = findOpenTile(openTiles, newCoordinate);

        if(openCoordinate.isPresent()){
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

    public Continent generateContinent(){
        Assert.isTrue(this.continent != null, "continent was nulll");
        continent.getCoordinates().addAll(coordinates);
        world.getCoordinates().addAll(coordinates);
        continent.getCoordinates().forEach(
                coordinate -> {
                    coordinate.setContinent(continent);
                    coordinate.getTile().setSurfaceType(continent.getType());
                }
        );

        return continent;
    }
}