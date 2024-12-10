package com.watchers.model.environment;

import com.watchers.TestableWorld;
import com.watchers.model.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TileTest {
    private static final long OCEAN_HEIGHT = 1000;
    private static final long SEA_HEIGHT = 2000;
    private static final long COASTAL_HEIGHT = 3000;
    private static final long PLAIN_HEIGHT = 4000;
    private static final long HILL_HEIGHT = 5000;
    private static final long MOUNTAIN_HEIGHT = 6000;

    Tile upperLeft;
    Tile upperMiddle;
    Tile upperRight;
    Tile middleLeft;
    Tile middleMiddle;
    Tile middleRight;
    Tile lowerLeft;
    Tile lowerMiddle;
    Tile lowerRight;

    World world;

    @BeforeEach
    void setupTest(){
        world = TestableWorld.createWorld();

        upperLeft = world.getCoordinate(1L, 1L).getTile();
        upperMiddle = world.getCoordinate(1L, 2L).getTile();
        upperRight = world.getCoordinate(1L, 3L).getTile();
        middleLeft = world.getCoordinate(2L, 1L).getTile();
        middleMiddle = world.getCoordinate(2L, 2L).getTile();
        middleRight = world.getCoordinate(2L, 3L).getTile();
        lowerLeft = world.getCoordinate(3L, 1L).getTile();
        lowerMiddle = world.getCoordinate(3L, 2L).getTile();
        lowerRight = world.getCoordinate(3L, 3L).getTile();

        world.getCoordinates().forEach(coordinate -> coordinate.getTile().setHeight(MOUNTAIN_HEIGHT));
        world.getCoordinates().forEach(coordinate -> coordinate.getTile().setSurfaceWater(1));
    }

    @Test
    void isLowestPointTest(){
        // check for all coordinates of equal height
        world.getCoordinates().forEach(coordinate -> assertTrue(coordinate.getTile().isLowestPoint()));

        // set middle coordinate lower so to make that the lowest tile for the four adjacent coordinates
        middleMiddle.setHeight(OCEAN_HEIGHT);

        assertTrue(middleMiddle.isLowestPoint());

        assertFalse(middleLeft.isLowestPoint());
        assertFalse(middleRight.isLowestPoint());
        assertFalse(lowerMiddle.isLowestPoint());
        assertFalse(upperMiddle.isLowestPoint());

        assertTrue(upperLeft.isLowestPoint());
        assertTrue(upperRight.isLowestPoint());
        assertTrue(lowerLeft.isLowestPoint());
        assertTrue(lowerRight.isLowestPoint());
    }

    @Test
    void hasLowerHeightNeighbour(){
        world.getCoordinates().forEach(coordinate -> assertFalse(coordinate.getTile().hasLowerHeightNeighbour()));

        // set middle coordinate lower so to make that the lowest tile for the four adjacent coordinates
        middleMiddle.setHeight(OCEAN_HEIGHT);

        assertFalse(middleMiddle.hasLowerHeightNeighbour());

        assertTrue(middleLeft.hasLowerHeightNeighbour());
        assertTrue(middleRight.hasLowerHeightNeighbour());
        assertTrue(lowerMiddle.hasLowerHeightNeighbour());
        assertTrue(upperMiddle.hasLowerHeightNeighbour());

        assertFalse(upperLeft.hasLowerHeightNeighbour());
        assertFalse(upperRight.hasLowerHeightNeighbour());
        assertFalse(lowerLeft.hasLowerHeightNeighbour());
        assertFalse(lowerRight.hasLowerHeightNeighbour());
    }

    @Test
    void getLowerHeightTilesOrderedByHeightDescendingTest(){
        upperLeft.setHeight(MOUNTAIN_HEIGHT);
        upperRight.setHeight(MOUNTAIN_HEIGHT);
        lowerLeft.setHeight(MOUNTAIN_HEIGHT);
        lowerRight.setHeight(MOUNTAIN_HEIGHT);
        upperMiddle.setHeight(MOUNTAIN_HEIGHT);

        middleLeft.setHeight(HILL_HEIGHT);
        middleRight.setHeight(HILL_HEIGHT);

        middleMiddle.setHeight(HILL_HEIGHT - 1);
        lowerMiddle.setHeight(PLAIN_HEIGHT);

        assertEquals(middleLeft, upperLeft.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertEquals(lowerMiddle, upperMiddle.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertEquals(middleRight, upperRight.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertEquals(middleMiddle, middleRight.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertEquals(lowerMiddle, middleMiddle.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertEquals(middleMiddle, middleRight.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertEquals(lowerMiddle, lowerLeft.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertEquals(lowerMiddle, lowerRight.getLowerHeightTilesOrderedByHeightDescending().get(0));
        assertTrue(lowerMiddle.getLowerHeightTilesOrderedByHeightDescending().isEmpty());

    }

}