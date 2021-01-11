package com.watchers.model.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.SurfaceType;
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

        coordinate1.getClimate().getIncomingCloud().addAirMoisture(1);
        coordinate2.getClimate().getIncomingCloud().addAirMoisture(2);
        coordinate3.getClimate().getIncomingCloud().addAirMoisture(3);
        coordinate4.getClimate().getIncomingCloud().addAirMoisture(4);

        world.getCoordinates().addAll(Arrays.asList(coordinate1, coordinate2, coordinate3, coordinate4));
        AirCurrent testCurrent = new AirCurrent(world.getCoordinates().stream().map(Coordinate::getClimate).collect(Collectors.toList()));

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());

        testCurrent.moveClouds();

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());


        testCurrent.moveClouds();

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());

        testCurrent.moveClouds();

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());


        testCurrent.moveClouds();

        assertEquals(4, testCurrent.getAirCurrentSize());
        assertEquals(1, testCurrent.getAirCurrentClimates().get(3).getCurrentCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(0).getCurrentCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(1).getCurrentCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(2).getCurrentCloud().getAirMoisture());

        assertEquals(1, testCurrent.getAirCurrentClimates().get(0).getIncomingCloud().getAirMoisture());
        assertEquals(2, testCurrent.getAirCurrentClimates().get(1).getIncomingCloud().getAirMoisture());
        assertEquals(3, testCurrent.getAirCurrentClimates().get(2).getIncomingCloud().getAirMoisture());
        assertEquals(4, testCurrent.getAirCurrentClimates().get(3).getIncomingCloud().getAirMoisture());
    }
}