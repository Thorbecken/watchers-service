package com.watchers.model;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class MockContinent {
    private List<Tile> tiles;
    private List<Tile> possibleTiles;
    private Continent continent;

    public MockContinent(Continent continent){
        this.tiles = new ArrayList<>();
        this.possibleTiles = new ArrayList<>();
        this.continent = continent;

        this.tiles.addAll(continent.getTiles());
        this.tiles.forEach(
                tile -> this.possibleTiles.addAll(tile.getNeighbours(this.continent))
        );
    }

    public void addRandomTile(WorldFactoryDTO dto) {
        List<Tile> openTiles = dto.getOpenTiles();
        List<Tile> takenTiles = dto.getTakenTiles();

        if (possibleTiles.isEmpty()){
            return;
        }
        Random random = new Random();
        int getInt = random.nextInt(possibleTiles.size());
        Tile newTile = possibleTiles.get(getInt);
        Optional<Tile> openTile = openTiles.stream().filter(
                tile -> tile.getCoordinate().equals(newTile.getCoordinate())
        ).findFirst();

        if(openTile.isPresent()){
            this.tiles.add(newTile);
            takenTiles.add(newTile);
            openTiles.remove(openTile.get());
            this.possibleTiles.addAll(newTile.getNeighbours(this.continent));
            this.possibleTiles.removeAll(this.tiles);
            this.possibleTiles = this.possibleTiles.stream()
                    .filter(tile -> takenTiles.stream().noneMatch(
                            takenTile -> tile.getCoordinate().equals(takenTile.getCoordinate())
                    )).collect(Collectors.toList());
        } else {
            possibleTiles.remove(newTile);
            this.addRandomTile(dto);
        }
    }

    public Continent generateContinent(){
        Continent continent = this.continent;
        continent.setTiles(new HashSet<>());
        this.tiles.forEach(
                mockTile -> continent.getTiles().add(new Tile(mockTile.getCoordinate().getXCoord(), mockTile.getCoordinate().getYCoord(), continent.getWorld() , continent))
        );
        return continent;
    }
}