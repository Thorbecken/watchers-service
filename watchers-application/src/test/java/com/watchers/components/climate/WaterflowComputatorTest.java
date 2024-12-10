package com.watchers.components.climate;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaterflowComputatorTest {
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
    WaterflowComputator waterflowComputator;

    @BeforeEach
    void setupTest(){
        world = TestableWorld.createWorld();
        TileDefined tileDefined = new TileDefined(OCEAN_HEIGHT, SEA_HEIGHT, COASTAL_HEIGHT, PLAIN_HEIGHT, HILL_HEIGHT, MOUNTAIN_HEIGHT);
        waterflowComputator = new WaterflowComputator(tileDefined);

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
        world.getCoordinates().forEach(coordinate -> coordinate.getTile().setRainfall(1));
    }

    @Test
    void processMountainLake() {
        world.getCoordinates().forEach(coordinate -> assertEquals(0, coordinate.getTile().getSurfaceWater()));
        world.getCoordinates().forEach(coordinate -> assertEquals(1, coordinate.getTile().getRainfall()));

        waterflowComputator.process(world);

        assertEquals(1,upperLeft.getSurfaceWater());
        assertEquals(1,upperMiddle.getSurfaceWater());
        assertEquals(1,upperLeft.getSurfaceWater());
        assertEquals(1,middleRight.getSurfaceWater());
        assertEquals(1,upperLeft.getSurfaceWater());
        assertEquals(1,middleLeft.getSurfaceWater());
        assertEquals(1, lowerLeft.getSurfaceWater());
        assertEquals(1,lowerMiddle.getSurfaceWater());
        assertEquals(1,lowerRight.getSurfaceWater());
    }

    @Test
    void processMiddleSlope() {
        upperLeft.setHeight(MOUNTAIN_HEIGHT);
        upperRight.setHeight(MOUNTAIN_HEIGHT);
        lowerLeft.setHeight(MOUNTAIN_HEIGHT);
        lowerRight.setHeight(MOUNTAIN_HEIGHT);
        upperMiddle.setHeight(MOUNTAIN_HEIGHT);

        middleLeft.setHeight(HILL_HEIGHT);
        middleRight.setHeight(HILL_HEIGHT);

        middleMiddle.setHeight(HILL_HEIGHT - 1);
        lowerMiddle.setHeight(PLAIN_HEIGHT);

        waterflowComputator.process(world);

//        Expected flow values with 1,1 as upper left coordinate
//        1,1,1
//        2,5,2
//        1,9,1

        assertEquals(1, upperLeft.getSurfaceWater());
        assertEquals(1, upperRight.getSurfaceWater());
        assertEquals(1, lowerLeft.getSurfaceWater());
        assertEquals(1, lowerRight.getSurfaceWater());
        assertEquals(1, upperMiddle.getSurfaceWater());

        assertEquals(2, middleLeft.getSurfaceWater());
        assertEquals(2, middleRight.getSurfaceWater());

        assertEquals(5, middleMiddle.getSurfaceWater());
        assertEquals(9, lowerMiddle.getSurfaceWater());
    }
}