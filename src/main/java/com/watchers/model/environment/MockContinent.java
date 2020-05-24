package com.watchers.model.environment;

import lombok.Data;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class MockContinent {
    private World world;
    private List<Tile> tiles;
    private List<Tile> possibleTiles;
    private Continent continent;

    public MockContinent(Continent continent){
        this.tiles = new ArrayList<>();
        this.possibleTiles = new ArrayList<>();
        this.continent = continent;
        this.world = continent.getWorld();

        this.tiles.addAll(continent.getTiles());
        this.tiles.forEach(
                tile -> this.possibleTiles.addAll(tile.getNeighboursContinental(this.continent))
        );
    }

    public void addRandomTile(WorldFactoryDTO dto) {
        if(possibleTiles.isEmpty()){
            return;
        }
        List<Tile> openTiles = dto.getOpenTiles();
        List<Tile> takenTiles = dto.getTakenTiles();

        int getInt = new Random().nextInt(possibleTiles.size());
        Tile newTile = possibleTiles.get(getInt);
        Optional<Tile> openTile = findOpenTile(openTiles, newTile);

        if(openTile.isPresent()){
            takeOpenTile(openTiles, takenTiles, newTile, openTile.get());
        } else {
            possibleTiles.remove(newTile);
            this.addRandomTile(dto);
        }
    }

    private Optional<Tile> findOpenTile(List<Tile> openTiles, Tile newTile) {
        return openTiles.stream().filter(
                tile -> tile.coordinateEquals(newTile)
        ).findFirst();
    }

    private void takeOpenTile(List<Tile> openTiles, List<Tile> takenTiles, Tile newTile, Tile openTile) {
        this.tiles.add(newTile);
        takenTiles.add(newTile);
        openTiles.remove(openTile);
        this.possibleTiles.addAll(newTile.getNeighboursContinental(this.continent));
        this.possibleTiles.removeAll(this.tiles);
        this.possibleTiles = this.possibleTiles.stream()
                .filter(tile -> takenTiles.stream().noneMatch(
                        tile::coordinateEquals
                )).collect(Collectors.toList());
    }

    public Continent generateContinent(){
        Continent continent = this.continent;
        continent.setTiles(new HashSet<>());
        this.tiles.forEach(
                mockTile -> continent.getTiles().add(new Tile(mockTile.getCoordinate(), continent.getWorld() , continent))
        );

        return continent;
    }
}