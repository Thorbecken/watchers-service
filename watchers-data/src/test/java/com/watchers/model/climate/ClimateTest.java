package com.watchers.model.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClimateTest {

    @Test
    void assertionsWater() {
        World world = new World(1,1);
        Continent continent = new Continent(world, SurfaceType.OCEAN);
        Coordinate coordinate = CoordinateFactory.createCoordinate(1,1, world, continent);
        Climate climate = coordinate.getClimate();

        assertTrue(climate.isWater());
        assertFalse(climate.isLand());
    }

    @Test
    void assertionsLand() {
        World world = new World(1,1);
        Continent continent = new Continent(world, SurfaceType.PLAIN);
        Coordinate coordinate = CoordinateFactory.createCoordinate(1,1, world, continent);
        Climate climate = coordinate.getClimate();

        assertTrue(climate.isLand());
        assertFalse(climate.isWater());
    }
}