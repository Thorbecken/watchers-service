package com.watchers.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class WorldFactoryDTO {

    private World world;
    private List<Tile> openTiles;
    private List<Tile> takenTiles;

    public WorldFactoryDTO(World world){
        this.world = world;
        this.takenTiles = new ArrayList<>();
        this.openTiles = generateOpenTiles(world);
    }

    private List<Tile> generateOpenTiles(World world) {
        List<Tile> openTiles = new ArrayList<>();
        Continent mockContinent = new Continent(world);
        mockContinent.setType(LandType.WATER);

        for (long xCoord = 1L; xCoord <= world.getXSize(); xCoord++){
            for (long yCoord = 1L; yCoord <= world.getYSize(); yCoord++){
                Tile tile = new Tile(xCoord, yCoord, world, mockContinent);
                tile.setLandType(LandType.WATER);
                openTiles.add(tile);
            }
        }

        List<Tile> startingTiles = openTiles.stream().filter(
                openTile -> world.getContinents().stream().anyMatch(
                        continent -> continent.getTiles().stream().anyMatch(
                                continentTile -> continentTile.getCoordinate().equals(openTile.getCoordinate())
                        )
                )
        ).collect(Collectors.toList());

        takenTiles.addAll(startingTiles);
        openTiles.removeAll(startingTiles);


        return openTiles.stream().filter(
                tile -> world.getContinents().stream().anyMatch(
                        continent -> continent.getTiles().stream().anyMatch(
                                continentTile -> !continentTile.getCoordinate().equals(tile.getCoordinate())
                        )
                )
        ).collect(Collectors.toList());
    }

}
