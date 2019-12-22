package com.watchers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    private World world;
    private Continent continent;

    @BeforeEach
    void setup(){
        this.continent = new Continent(world);

        this.world = new World(3, 3);
        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                world.getTiles().add(new Tile(x,y,world, continent));
            }
        }
    }

    @ParameterizedTest
    @SuppressWarnings("all")
    @CsvSource({"1,1", "1,2", "1,3", "2,1", "2,2", "2,3", "3,1", "3,3", "3,3"})
    void getNeighbours(long x, long y) {
        Tile tile = world.getTiles().stream().filter(worldTile -> worldTile.getXCoord() == x && worldTile.getYCoord() == y).findFirst().get();
        List<Tile> neighbours = tile.getNeighbours(continent);

        if(y == 1 || y == 3){
            assertEquals(3, neighbours.size());
        } else {
            assertEquals(4, neighbours.size());
        }
    }
}