package com.watchers.model.climate;

import com.watchers.helper.SkyHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SkyTileTest {

    @Test
    void moveClouds() {
        World world = new World(4,3);
        Continent continent = new Continent(world, SurfaceType.PLAIN);
        Coordinate coordinate1 = CoordinateFactory.createCoordinate(1,2, world, continent);
        Coordinate coordinate2 = CoordinateFactory.createCoordinate(2,2, world, continent);
        Coordinate coordinate3 = CoordinateFactory.createCoordinate(3,2, world, continent);
        Coordinate coordinate4 = CoordinateFactory.createCoordinate(4,2, world, continent);

        world.getCoordinates().addAll(Arrays.asList(coordinate1, coordinate2, coordinate3, coordinate4));

        SkyHelper.calculateAirflows(world);

        SkyTile skyTile1 = coordinate1.getClimate().getSkyTile();
        SkyTile skyTile2 = coordinate2.getClimate().getSkyTile();
        SkyTile skyTile3 = coordinate3.getClimate().getSkyTile();
        SkyTile skyTile4 = coordinate4.getClimate().getSkyTile();

        List<SkyTile> skyTileList = Arrays.asList(skyTile1, skyTile2, skyTile3, skyTile4);

        // preperation for heightdifference check
        coordinate1.getTile().setHeight(2);
        coordinate2.getTile().setHeight(4);
        coordinate3.getTile().setHeight(8);
        coordinate4.getTile().setHeight(0);

        assertEquals(0, skyTile1.getAirMoisture());
        assertEquals(0, skyTile2.getAirMoisture());
        assertEquals(0, skyTile3.getAirMoisture());
        assertEquals(0, skyTile4.getAirMoisture());


        skyTile1.addAirMoisture(1);
        skyTile2.addAirMoisture(2);
        skyTile3.addAirMoisture(3);
        skyTile4.addAirMoisture(4);

        world.getCoordinates().addAll(Arrays.asList(coordinate1, coordinate2, coordinate3, coordinate4));

        assertEquals(1, skyTile1.getAirMoisture());
        assertEquals(2, skyTile2.getAirMoisture());
        assertEquals(3, skyTile3.getAirMoisture());
        assertEquals(4, skyTile4.getAirMoisture());

        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);

        assertEquals(4, skyTile1.getAirMoisture());
        assertEquals(1, skyTile2.getAirMoisture());
        assertEquals(2, skyTile3.getAirMoisture());
        assertEquals(3, skyTile4.getAirMoisture());

        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);

        assertEquals(3, skyTile1.getAirMoisture());
        assertEquals(4, skyTile2.getAirMoisture());
        assertEquals(1, skyTile3.getAirMoisture());
        assertEquals(2, skyTile4.getAirMoisture());

        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);

        assertEquals(2, skyTile1.getAirMoisture());
        assertEquals(3, skyTile2.getAirMoisture());
        assertEquals(4, skyTile3.getAirMoisture());
        assertEquals(1, skyTile4.getAirMoisture());

        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);

        assertEquals(1, skyTile1.getAirMoisture());
        assertEquals(2, skyTile2.getAirMoisture());
        assertEquals(3, skyTile3.getAirMoisture());
        assertEquals(4, skyTile4.getAirMoisture());
    }
}