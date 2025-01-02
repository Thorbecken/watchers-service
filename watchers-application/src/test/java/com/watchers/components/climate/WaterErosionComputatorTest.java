package com.watchers.components.climate;

import com.watchers.TestableWorld;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaterErosionComputatorTest {
    private static final long OCEAN_HEIGHT = 1000;
    private static final long PLAIN_HEIGHT = 4000;
    private static final long HILL_HEIGHT = 5000;
    private static final long MOUNTAIN_HEIGHT = 6000;
    private static final int MULTIPLIER = 1;

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
    WaterErosionComputator waterErosionComputator;
    ContinentalDriftTaskDto continentalDriftTaskDto;

    @BeforeEach
    void setupTest() {
        world = TestableWorld.createWorld();
        continentalDriftTaskDto = new ContinentalDriftTaskDto(world.getWorldMetaData());
        continentalDriftTaskDto.setWorld(world);
        waterErosionComputator = new WaterErosionComputator(1, MULTIPLIER);

        upperLeft = world.getCoordinate(1L, 1L).getTile();
        upperMiddle = world.getCoordinate(1L, 2L).getTile();
        upperRight = world.getCoordinate(1L, 3L).getTile();
        middleLeft = world.getCoordinate(2L, 1L).getTile();
        middleMiddle = world.getCoordinate(2L, 2L).getTile();
        middleRight = world.getCoordinate(2L, 3L).getTile();
        lowerLeft = world.getCoordinate(3L, 1L).getTile();
        lowerMiddle = world.getCoordinate(3L, 2L).getTile();
        lowerRight = world.getCoordinate(3L, 3L).getTile();

        world.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.setSurfaceType(SurfaceType.MOUNTAIN));
    }

    @Test
    void processTest() {
        upperLeft.setHeight(MOUNTAIN_HEIGHT);
        upperLeft.setSurfaceWater(1);
        upperLeft.setDownWardTile(middleLeft);

        upperRight.setHeight(MOUNTAIN_HEIGHT);
        upperRight.setSurfaceWater(1);
        upperRight.setDownWardTile(middleRight);

        lowerLeft.setHeight(MOUNTAIN_HEIGHT);
        lowerLeft.setSurfaceWater(1);
        lowerLeft.setDownWardTile(lowerMiddle);

        lowerRight.setHeight(MOUNTAIN_HEIGHT);
        lowerRight.setSurfaceWater(1);
        lowerRight.setDownWardTile(lowerMiddle);

        upperMiddle.setHeight(MOUNTAIN_HEIGHT);
        upperMiddle.setSurfaceWater(1);
        upperMiddle.setDownWardTile(lowerMiddle);

        middleLeft.setHeight(HILL_HEIGHT);
        middleLeft.setSurfaceWater(2);
        middleLeft.setDownWardTile(middleMiddle);

        middleRight.setHeight(HILL_HEIGHT);
        middleRight.setSurfaceWater(2);
        middleRight.setDownWardTile(middleMiddle);

        middleMiddle.setHeight(HILL_HEIGHT - 200);
        middleMiddle.setSurfaceWater(5);
        middleMiddle.setDownWardTile(lowerMiddle);

        lowerMiddle.setHeight(OCEAN_HEIGHT);
        lowerMiddle.setSurfaceType(SurfaceType.OCEAN);

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getDownWardTile() != null)
                .forEach(tile -> tile.getDownWardTile().getUpwardTiles().add(tile));

        Function<Tile, Double> expectedHeightCalculator = (Tile tile) -> tile.getHeight() + tile.getUpwardTiles().stream().mapToDouble(Tile::getSurfaceWater).sum() - tile.getSurfaceWater() * MULTIPLIER;

        double upperLeftHeightChange = expectedHeightCalculator.apply(upperLeft);
        double upperMiddleHeightChange = expectedHeightCalculator.apply(upperMiddle);
        double upperRightHeightChange = expectedHeightCalculator.apply(upperRight);
        double middleLeftHeightChange = expectedHeightCalculator.apply(middleLeft);
        double middleMiddleHeightChange = expectedHeightCalculator.apply(middleMiddle);
        double middleRightHeightChange = expectedHeightCalculator.apply(middleRight);
        double lowerLeftHeightChange = expectedHeightCalculator.apply(lowerLeft);
        double lowerMiddleHeightChange = expectedHeightCalculator.apply(lowerMiddle);
        double lowerRightHeightChange = expectedHeightCalculator.apply(lowerRight);

        waterErosionComputator.process(world);

        assertEquals(upperLeftHeightChange, upperLeft.getHeight());
        assertEquals(upperMiddleHeightChange, upperMiddle.getHeight());
        assertEquals(upperRightHeightChange, upperRight.getHeight());
        assertEquals(middleLeftHeightChange, middleLeft.getHeight());
        assertEquals(middleMiddleHeightChange, middleMiddle.getHeight());
        assertEquals(middleRightHeightChange, middleRight.getHeight());
        assertEquals(lowerLeftHeightChange, lowerLeft.getHeight());
        assertEquals(lowerMiddleHeightChange, lowerMiddle.getHeight());
        assertEquals(lowerRightHeightChange, lowerRight.getHeight());
    }

    @Test
    void processTestSameHeight() {
        upperLeft.setHeight(MOUNTAIN_HEIGHT);
        upperLeft.setSurfaceWater(1);
        upperLeft.setDownWardTile(upperRight);

        upperRight.setHeight(MOUNTAIN_HEIGHT);

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getDownWardTile() != null)
                .forEach(tile -> tile.getDownWardTile().getUpwardTiles().add(tile));

        waterErosionComputator.process(world);

        assertEquals(MOUNTAIN_HEIGHT, upperLeft.getHeight());
        assertEquals(MOUNTAIN_HEIGHT, upperRight.getHeight());
    }

    @Test
    void processTestMax() {
        upperLeft.setHeight(MOUNTAIN_HEIGHT);
        upperLeft.setSurfaceWater(10000);
        upperLeft.setDownWardTile(upperRight);

        upperRight.setHeight(PLAIN_HEIGHT);

        double averageHeight = (double) (MOUNTAIN_HEIGHT + PLAIN_HEIGHT) /2;

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getDownWardTile() != null)
                .forEach(tile -> tile.getDownWardTile().getUpwardTiles().add(tile));

        waterErosionComputator.process(world);

        assertEquals(averageHeight, upperLeft.getHeight());
        assertEquals(averageHeight, upperRight.getHeight());
    }

    @Test
    void processTestMin() {
        upperLeft.setHeight(MOUNTAIN_HEIGHT);
        upperLeft.setSurfaceWater(0);
        upperLeft.setDownWardTile(upperRight);

        upperRight.setHeight(PLAIN_HEIGHT);

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getDownWardTile() != null)
                .forEach(tile -> tile.getDownWardTile().getUpwardTiles().add(tile));

        waterErosionComputator.process(world);

        assertEquals(MOUNTAIN_HEIGHT, upperLeft.getHeight());
        assertEquals(PLAIN_HEIGHT, upperRight.getHeight());
    }

    @Test
    void processTestLowerHeight() {
        upperLeft.setHeight(PLAIN_HEIGHT);
        upperLeft.setSurfaceWater(100000);
        upperLeft.setDownWardTile(upperRight);

        upperRight.setHeight(MOUNTAIN_HEIGHT);
        upperRight.setSurfaceWater(1);

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getDownWardTile() != null)
                .forEach(tile -> tile.getDownWardTile().getUpwardTiles().add(tile));

        waterErosionComputator.process(world);

        assertEquals(PLAIN_HEIGHT, upperLeft.getHeight());
        assertEquals(MOUNTAIN_HEIGHT, upperRight.getHeight());
    }


}