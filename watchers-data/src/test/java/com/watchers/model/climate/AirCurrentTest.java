package com.watchers.model.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AirCurrentTest {

    @Test
    void moveClouds() {
        World world = new World(4,3);
        Continent continent = new Continent(world, SurfaceType.PLAIN);
        Coordinate coordinate1 = CoordinateFactory.createCoordinate(1,2, world, continent);
        Coordinate coordinate2 = CoordinateFactory.createCoordinate(2,2, world, continent);
        Coordinate coordinate3 = CoordinateFactory.createCoordinate(3,2, world, continent);
        Coordinate coordinate4 = CoordinateFactory.createCoordinate(4,2, world, continent);

        // preperation for heightdifference check
        coordinate1.getTile().setHeight(2);
        coordinate2.getTile().setHeight(4);
        coordinate3.getTile().setHeight(8);
        coordinate4.getTile().setHeight(0);

        coordinate1.getClimate().getCurrentCloud().addAirMoisture(1);
        coordinate2.getClimate().getCurrentCloud().addAirMoisture(2);
        coordinate3.getClimate().getCurrentCloud().addAirMoisture(3);
        coordinate4.getClimate().getCurrentCloud().addAirMoisture(4);

        world.getCoordinates().addAll(Arrays.asList(coordinate1, coordinate2, coordinate3, coordinate4));
        AirCurrent testCurrent = new AirCurrent(world.getCoordinates().stream().map(Coordinate::getClimate).collect(Collectors.toList()));

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());

        testCurrent.moveClouds();

        // heightdifference control
        assertEquals(0, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getPreviousHeight());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getPreviousHeight());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getPreviousHeight());
        assertEquals(8, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getPreviousHeight());

        assertEquals(2, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getCurrentHeight());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getCurrentHeight());
        assertEquals(8, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getCurrentHeight());
        assertEquals(0, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getCurrentHeight());



        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());


        testCurrent.moveClouds();

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());

        testCurrent.moveClouds();

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());


        testCurrent.moveClouds();

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
    }
}