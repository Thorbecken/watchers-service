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
    private List<Coordinate> coordinates;
    private List<Coordinate> possibleCoordinates;
    private Continent continent;

    public MockContinent(Continent continent){
        this.coordinates = new ArrayList<>();
        this.possibleCoordinates = new ArrayList<>();
        this.continent = continent;
        this.world = continent.getWorld();

        this.coordinates.addAll(continent.getTiles().stream().map(Tile::getCoordinate).collect(Collectors.toList()));
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

    public MockContinent(Coordinate coordinate){
        Assert.isTrue(coordinate.getWorld() != null, "no world found!");

        this.coordinates = new ArrayList<>();
        this.possibleCoordinates = new ArrayList<>();
        this.world = coordinate.getWorld();

        this.coordinates.addAll(Collections.singleton(coordinate));
        this.coordinates.forEach(tile -> this.possibleCoordinates.addAll(tile.getNeighbours())
        );
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

    private void takeOpenTile(List<Coordinate> openTiles, List<Coordinate> takenTiles, Coordinate newTile, Coordinate openTile) {
        this.coordinates.add(newTile);
        takenTiles.add(newTile);
        openTiles.remove(openTile);
        this.possibleCoordinates.addAll(newTile.getNeighbours());
        this.possibleCoordinates.removeAll(this.coordinates);
        this.possibleCoordinates = this.possibleCoordinates.stream()
                .filter(tile -> takenTiles.stream().noneMatch(
                        tile::equals
                )).collect(Collectors.toList());
    }

    public Continent generateContinent(){
        Assert.isTrue(this.continent != null, "continent was nulll");
        Continent continent = this.continent;
        continent.setTiles(new HashSet<>());
        this.coordinates.forEach(
                coordinate -> continent.getTiles().add(new Tile(coordinate, continent.getWorld() , continent))
        );

        return continent;
    }
}