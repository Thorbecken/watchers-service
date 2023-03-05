package com.watchers.model.coordinate;

import com.watchers.TestableWorld;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.BiPredicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinateTest {

    @Test
    void testCalculation() {
        World world = new World(58, 28);

        Coordinate coordinate = CoordinateFactory.createCoordinate(30, 10, world, new Continent(world, SurfaceType.OCEAN));
        Coordinate soughtCoordinate = CoordinateFactory.createCoordinate(30, 9, world, new Continent(world, SurfaceType.COASTAL));
        world.getCoordinates().add(soughtCoordinate);

        Coordinate distantCoordinate = coordinate.calculateDistantCoordinate(0, -1);

        assertEquals(9, distantCoordinate.getYCoord());
    }

    @Test
    public void getRightNeighbourTest() {
        World world = TestableWorld.createWorld();
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Coordinate rightCoordinate = startCoordinate.getRightNeighbour();

        assertThat(rightCoordinate.getYCoord(), equalTo(startCoordinate.getYCoord()));
        assertThat(rightCoordinate.getXCoord(), equalTo(3L));

        startCoordinate = world.getCoordinate(3L, 2L);
        rightCoordinate = startCoordinate.getRightNeighbour();

        assertThat(rightCoordinate.getYCoord(), equalTo(startCoordinate.getYCoord()));
        assertThat(rightCoordinate.getXCoord(), equalTo(1L));
    }

    @Test
    public void getLeftNeighbourTest() {
        World world = TestableWorld.createWorld();
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Coordinate leftNeighbour = startCoordinate.getLeftNeighbour();

        assertThat(leftNeighbour.getYCoord(), equalTo(startCoordinate.getYCoord()));
        assertThat(leftNeighbour.getXCoord(), equalTo(1L));

        startCoordinate = world.getCoordinate(1L, 2L);
        leftNeighbour = startCoordinate.getLeftNeighbour();

        assertThat(leftNeighbour.getYCoord(), equalTo(startCoordinate.getYCoord()));
        assertThat(leftNeighbour.getXCoord(), equalTo(3L));
    }

    @Test
    public void getUpNeighbourTest() {
        World world = TestableWorld.createWorld();
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Coordinate upNeighbour = startCoordinate.getUpNeighbour();

        assertThat(upNeighbour.getXCoord(), equalTo(startCoordinate.getXCoord()));
        assertThat(upNeighbour.getYCoord(), equalTo(3L));

        startCoordinate = world.getCoordinate(2L, 3L);
        upNeighbour = startCoordinate.getUpNeighbour();

        assertThat(upNeighbour.getXCoord(), equalTo(startCoordinate.getXCoord()));
        assertThat(upNeighbour.getYCoord(), equalTo(1L));
    }

    @Test
    public void getDownNeighbourTest() {
        World world = TestableWorld.createWorld();
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Coordinate rightCoordinate = startCoordinate.getDownNeighbour();

        assertThat(rightCoordinate.getXCoord(), equalTo(startCoordinate.getXCoord()));
        assertThat(rightCoordinate.getYCoord(), equalTo(1L));

        startCoordinate = world.getCoordinate(2L, 1L);
        rightCoordinate = startCoordinate.getDownNeighbour();

        assertThat(rightCoordinate.getXCoord(), equalTo(startCoordinate.getXCoord()));
        assertThat(rightCoordinate.getYCoord(), equalTo(3L));
    }

    @Test
    public void getCoordinatesWithinRangeWithoutPredicateTest() {
        World world = TestableWorld.createMediumWorld();
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Set<Coordinate> coordinatesInRange = startCoordinate.getCoordinatesWithinRange(1);
        assertThat(coordinatesInRange, hasSize(4));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));

        coordinatesInRange = startCoordinate.getCoordinatesWithinRange(2);
        assertThat(coordinatesInRange, hasSize(10));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));
    }

    @Test
    public void lowerOrEqualHeightPredicateTest(){
        World world = TestableWorld.createWorld();
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Coordinate endCoordinate = world.getCoordinate(1L, 2L);

        startCoordinate.getTile().setHeight(10);
        endCoordinate.getTile().setHeight(20);
        assertThat(Coordinate.LOWER_OR_EQUAL_HEIGHT_PREDICATE.test(startCoordinate, endCoordinate), equalTo(false));
        assertThat(Coordinate.LOWER_OR_EQUAL_HEIGHT_PREDICATE.test(endCoordinate, startCoordinate), equalTo(true));

        endCoordinate.getTile().setHeight(10);
        assertThat(Coordinate.LOWER_OR_EQUAL_HEIGHT_PREDICATE.test(startCoordinate, endCoordinate), equalTo(true));
        assertThat(Coordinate.LOWER_OR_EQUAL_HEIGHT_PREDICATE.test(endCoordinate, startCoordinate), equalTo(true));
    }

    @Test
    public void getLowerOrEqualHeightCoordinatesWithinRangeTest() {
        World world = TestableWorld.createWorld();
        world.getCoordinate(1L, 1L).getTile().setHeight(10);
        world.getCoordinate(1L, 2L).getTile().setHeight(10);
        world.getCoordinate(1L, 3L).getTile().setHeight(10);
        world.getCoordinate(2L, 1L).getTile().setHeight(10);
        world.getCoordinate(2L, 2L).getTile().setHeight(10);
        world.getCoordinate(2L, 3L).getTile().setHeight(10);
        world.getCoordinate(3L, 1L).getTile().setHeight(10);
        world.getCoordinate(3L, 2L).getTile().setHeight(10);
        world.getCoordinate(3L, 3L).getTile().setHeight(10);
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Set<Coordinate> coordinatesInRange = startCoordinate.getLowerOrEqualHeightLandCoordinatesWithinRange(1);
        assertThat(coordinatesInRange, hasSize(2));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));

        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .forEach(tile -> tile.setSurfaceType(SurfaceType.PLAIN));
        coordinatesInRange = startCoordinate.getLowerOrEqualHeightLandCoordinatesWithinRange(1);
        assertThat(coordinatesInRange, hasSize(4));

        coordinatesInRange = startCoordinate.getLowerOrEqualHeightLandCoordinatesWithinRange(2);
        assertThat(coordinatesInRange, hasSize(8));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));

        world.getCoordinate(1L, 1L).getTile().setHeight(60);
        world.getCoordinate(1L, 2L).getTile().setHeight(60);
        world.getCoordinate(1L, 3L).getTile().setHeight(60);
        world.getCoordinate(2L, 1L).getTile().setHeight(60);
        world.getCoordinate(3L, 1L).getTile().setHeight(60);
        world.getCoordinate(3L, 2L).getTile().setHeight(60);

        coordinatesInRange = startCoordinate.getLowerOrEqualHeightLandCoordinatesWithinRange(1);
        assertThat(coordinatesInRange, hasSize(1));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));

        coordinatesInRange = startCoordinate.getLowerOrEqualHeightLandCoordinatesWithinRange(2);
        assertThat(coordinatesInRange, hasSize(2));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));
    }

    @Test
    public void getCoordinatesWithinRangeWithQualifierTest() {
        BiPredicate<Coordinate, Coordinate> predicate = (x, y) -> y.getTile().getHeight() <= x.getTile().getHeight();
        World world = TestableWorld.createWorld();
        world.getCoordinate(1L, 1L).getTile().setHeight(10);
        world.getCoordinate(1L, 2L).getTile().setHeight(10);
        world.getCoordinate(1L, 3L).getTile().setHeight(10);
        world.getCoordinate(2L, 1L).getTile().setHeight(10);
        world.getCoordinate(2L, 2L).getTile().setHeight(10);
        world.getCoordinate(2L, 3L).getTile().setHeight(10);
        world.getCoordinate(3L, 1L).getTile().setHeight(10);
        world.getCoordinate(3L, 2L).getTile().setHeight(10);
        world.getCoordinate(3L, 3L).getTile().setHeight(10);
        Coordinate startCoordinate = world.getCoordinate(2L, 2L);
        Set<Coordinate> coordinatesInRange = startCoordinate.getCoordinatesWithinRangeWithQualifier(1, predicate);
        assertThat(coordinatesInRange, hasSize(4));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));

        coordinatesInRange = startCoordinate.getCoordinatesWithinRangeWithQualifier(2, predicate);
        assertThat(coordinatesInRange, hasSize(8));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));

        world.getCoordinate(1L, 1L).getTile().setHeight(60);
        world.getCoordinate(1L, 2L).getTile().setHeight(60);
        world.getCoordinate(1L, 3L).getTile().setHeight(60);
        world.getCoordinate(2L, 1L).getTile().setHeight(60);
        world.getCoordinate(3L, 1L).getTile().setHeight(60);
        world.getCoordinate(3L, 2L).getTile().setHeight(60);

        coordinatesInRange = startCoordinate.getCoordinatesWithinRangeWithQualifier(1, predicate);
        assertThat(coordinatesInRange, hasSize(1));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));

        coordinatesInRange = startCoordinate.getCoordinatesWithinRangeWithQualifier(2, predicate);
        assertThat(coordinatesInRange, hasSize(2));
        assertThat(coordinatesInRange.contains(startCoordinate), equalTo(false));
    }

}