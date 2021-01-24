package com.watchers.model.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloudTest {

    @Test
    void addAirMoistureAndReduceAirMoistureTest() {
        Cloud cloud = new Cloud();
        assertEquals(0, cloud.getAirMoisture());

        cloud.addAirMoisture(50);
        assertEquals(50, cloud.getAirMoisture());

        cloud.addAirMoisture(50);
        assertEquals(100, cloud.getAirMoisture());

        cloud.addAirMoisture(50);
        assertEquals(100, cloud.getAirMoisture());

        cloud.reduceAirMoisture(50);
        assertEquals(50, cloud.getAirMoisture());

        cloud.reduceAirMoisture(50);
        assertEquals(0, cloud.getAirMoisture());

        cloud.reduceAirMoisture(50);
        assertEquals(0, cloud.getAirMoisture());
    }

    @Test
    void calculateHeightDifferenceEffectTest(){
        World world = new World(1,3);
        Continent continent = new Continent(world, SurfaceType.PLAIN);
        Coordinate coordinate1 = CoordinateFactory.createCoordinate(1,1, world, continent);
        Coordinate coordinate2 = CoordinateFactory.createCoordinate(1,2, world, continent);
        Coordinate coordinate3 = CoordinateFactory.createCoordinate(1,3, world, continent);

        coordinate1.getTile().setHeight(1);
        coordinate2.getTile().setHeight(2);
        coordinate3.getTile().setHeight(3);

        Cloud cloud1 = coordinate1.getClimate().getCurrentCloud();
        Cloud cloud2 = coordinate2.getClimate().getCurrentCloud();
        Cloud cloud3 = coordinate3.getClimate().getCurrentCloud();

        cloud1.setPreviousHeight();
        cloud2.setPreviousHeight();
        cloud3.setPreviousHeight();

        assertEquals(1, cloud1.getPreviousHeight());
        assertEquals(2, cloud2.getPreviousHeight());
        assertEquals(3, cloud3.getPreviousHeight());

        coordinate1.getClimate().setCurrentCloud(cloud3);
        coordinate2.getClimate().setCurrentCloud(cloud1);
        coordinate3.getClimate().setCurrentCloud(cloud2);

        cloud1.setCurrentHeight();
        cloud2.setCurrentHeight();
        cloud3.setCurrentHeight();

        assertEquals(2, cloud1.getCurrentHeight());
        assertEquals(3, cloud2.getCurrentHeight());
        assertEquals(1, cloud3.getCurrentHeight());

        cloud1.calculateHeightDifferenceEffect();
        // adding moisture
        cloud2.addAirMoisture(1);
        cloud2.calculateHeightDifferenceEffect();
        cloud3.calculateHeightDifferenceEffect();

        // no increase of height, so no increase of airMoistureLossage
        assertEquals(0, cloud1.getAirMoistureLossage());
        // increase in height and has moisture
        assertEquals(1, cloud2.getAirMoistureLossage());
        // increase in height and no moisture
        assertEquals(0, cloud3.getAirMoistureLossage());

    }
}