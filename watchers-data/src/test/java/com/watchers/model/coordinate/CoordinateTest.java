package com.watchers.model.coordinate;

import com.watchers.TestableWorld;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CoordinateTest {

    @Test
    public void getRightNeighbourTest(){
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
    public void getLeftNeighbourTest(){
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
    public void getUpNeighbourTest(){
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
    public void getDownNeighbourTest(){
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

}