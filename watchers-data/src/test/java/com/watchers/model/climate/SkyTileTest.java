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
    void moveCloudsSimmple(){
        World world = new World();
        world.setXSize(2L);
        world.setYSize(2L);
        Continent continent = new Continent(world, SurfaceType.OCEAN);

        SkyTile sky1 = CoordinateFactory.createCoordinate(1, 1, world, continent).getClimate().getSkyTile();
        SkyTile sky2 = CoordinateFactory.createCoordinate(2, 1, world, continent).getClimate().getSkyTile();
        SkyTile sky3 = CoordinateFactory.createCoordinate(1, 2, world, continent).getClimate().getSkyTile();
        SkyTile sky4 = CoordinateFactory.createCoordinate(2, 2, world, continent).getClimate().getSkyTile();
        SkyHelper.calculateAirflows(world);

        List<SkyTile> skyTileList = Arrays.asList(
                sky1, sky2, sky3, sky4
        );

        skyTileList.forEach(skyTile -> {
            assertEquals(2, skyTile.getOutgoingAircurrents().size());
            skyTile.addAirMoisture(2);
            skyTile.getOutgoingAircurrents().forEach(aircurrent -> aircurrent.setCurrentStrength(1));
        });

        assertEquals(2, sky1.getAirMoisture());
        assertEquals(2, sky2.getAirMoisture());
        assertEquals(2, sky3.getAirMoisture());
        assertEquals(2, sky4.getAirMoisture());

        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);

        assertEquals(2, sky1.getAirMoisture());
        assertEquals(2, sky2.getAirMoisture());
        assertEquals(2, sky3.getAirMoisture());
        assertEquals(2, sky4.getAirMoisture());
    }

    @Test
    void moveClouds() {
        World world = new World();
        world.setXSize(5L);
        world.setYSize(6L);
        Continent continent = new Continent(world, SurfaceType.OCEAN);

        SkyTile sky1 = CoordinateFactory.createCoordinate(1, 1, world, continent).getClimate().getSkyTile();
        SkyTile sky2 = CoordinateFactory.createCoordinate(2, 1, world, continent).getClimate().getSkyTile();
        SkyTile sky3 = CoordinateFactory.createCoordinate(3, 1, world, continent).getClimate().getSkyTile();
        SkyTile sky4 = CoordinateFactory.createCoordinate(4, 1, world, continent).getClimate().getSkyTile();
        SkyTile sky5 = CoordinateFactory.createCoordinate(5, 1, world, continent).getClimate().getSkyTile();

        SkyTile sky6 = CoordinateFactory.createCoordinate(1, 2, world, continent).getClimate().getSkyTile();
        SkyTile sky7 = CoordinateFactory.createCoordinate(2, 2, world, continent).getClimate().getSkyTile();
        SkyTile sky8 = CoordinateFactory.createCoordinate(3, 2, world, continent).getClimate().getSkyTile();
        SkyTile sky9 = CoordinateFactory.createCoordinate(4, 2, world, continent).getClimate().getSkyTile();
        SkyTile sky10 = CoordinateFactory.createCoordinate(5, 2, world, continent).getClimate().getSkyTile();

        SkyTile sky11 = CoordinateFactory.createCoordinate(1, 3, world, continent).getClimate().getSkyTile();
        SkyTile sky12 = CoordinateFactory.createCoordinate(2, 3, world, continent).getClimate().getSkyTile();
        SkyTile sky13 = CoordinateFactory.createCoordinate(3, 3, world, continent).getClimate().getSkyTile();
        SkyTile sky14 = CoordinateFactory.createCoordinate(4, 3, world, continent).getClimate().getSkyTile();
        SkyTile sky15 = CoordinateFactory.createCoordinate(5, 3, world, continent).getClimate().getSkyTile();

        SkyTile sky16 = CoordinateFactory.createCoordinate(1, 4, world, continent).getClimate().getSkyTile();
        SkyTile sky17 = CoordinateFactory.createCoordinate(2, 4, world, continent).getClimate().getSkyTile();
        SkyTile sky18 = CoordinateFactory.createCoordinate(3, 4, world, continent).getClimate().getSkyTile();
        SkyTile sky19 = CoordinateFactory.createCoordinate(4, 4, world, continent).getClimate().getSkyTile();
        SkyTile sky20 = CoordinateFactory.createCoordinate(5, 4, world, continent).getClimate().getSkyTile();

        SkyTile sky21 = CoordinateFactory.createCoordinate(1, 5, world, continent).getClimate().getSkyTile();
        SkyTile sky22 = CoordinateFactory.createCoordinate(2, 5, world, continent).getClimate().getSkyTile();
        SkyTile sky23 = CoordinateFactory.createCoordinate(3, 5, world, continent).getClimate().getSkyTile();
        SkyTile sky24 = CoordinateFactory.createCoordinate(4, 5, world, continent).getClimate().getSkyTile();
        SkyTile sky25 = CoordinateFactory.createCoordinate(5, 5, world, continent).getClimate().getSkyTile();

        SkyTile sky26 = CoordinateFactory.createCoordinate(1, 6, world, continent).getClimate().getSkyTile();
        SkyTile sky27 = CoordinateFactory.createCoordinate(2, 6, world, continent).getClimate().getSkyTile();
        SkyTile sky28 = CoordinateFactory.createCoordinate(3, 6, world, continent).getClimate().getSkyTile();
        SkyTile sky29 = CoordinateFactory.createCoordinate(4, 6, world, continent).getClimate().getSkyTile();
        SkyTile sky30 = CoordinateFactory.createCoordinate(5, 6, world, continent).getClimate().getSkyTile();

        SkyHelper.calculateAirflows(world);

        List<SkyTile> skyTileList = Arrays.asList(
                sky1, sky2, sky3, sky4, sky5,
                sky6, sky7, sky8, sky9, sky10,
                sky11, sky12, sky13, sky14, sky15,
                sky16, sky17, sky18, sky19, sky20,
                sky21, sky22, sky23, sky24, sky25,
                sky26, sky27, sky28, sky29, sky30
        );

        skyTileList.forEach(skyTile -> assertEquals(2, skyTile.getOutgoingAircurrents().size()));

        // preperation for heightdifference check
        sky1.getClimate().getCoordinate().getTile().setHeight(2);
        sky2.getClimate().getCoordinate().getTile().setHeight(4);
        sky3.getClimate().getCoordinate().getTile().setHeight(8);
        sky4.getClimate().getCoordinate().getTile().setHeight(0);
        sky5.getClimate().getCoordinate().getTile().setHeight(0);

        assertEquals(0, sky1.getAirMoisture());
        assertEquals(0, sky2.getAirMoisture());
        assertEquals(0, sky3.getAirMoisture());
        assertEquals(0, sky4.getAirMoisture());
        assertEquals(0, sky5.getAirMoisture());

        skyTileList.forEach(skyTile -> {
                    long xCoord = skyTile.getClimate().getCoordinate().getXCoord();
                    if (xCoord == 1) {
                        skyTile.addAirMoisture(2);
                    } else if (xCoord == 2) {
                        skyTile.addAirMoisture(4);
                    } else if (xCoord == 3) {
                        skyTile.addAirMoisture(6);
                    } else if (xCoord == 4) {
                        skyTile.addAirMoisture(8);
                    } else if (xCoord == 5) {
                        skyTile.addAirMoisture(10);
                    }
                }
        );

        assertEquals(2, sky1.getAirMoisture());
        assertEquals(4, sky2.getAirMoisture());
        assertEquals(6, sky3.getAirMoisture());
        assertEquals(8, sky4.getAirMoisture());
        assertEquals(10, sky5.getAirMoisture());

        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);

        assertEquals(2, sky1.getAirMoisture());
        assertEquals(3, sky2.getAirMoisture());
        assertEquals(4, sky3.getAirMoisture());
        assertEquals(5, sky4.getAirMoisture());
        assertEquals(1, sky5.getAirMoisture());
//
//        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
//        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);
//
//        assertEquals(3, skyTile1.getAirMoisture());
//        assertEquals(4, skyTile2.getAirMoisture());
//        assertEquals(1, skyTile3.getAirMoisture());
//        assertEquals(2, skyTile4.getAirMoisture());
//
//        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
//        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);
//
//        assertEquals(2, skyTile1.getAirMoisture());
//        assertEquals(3, skyTile2.getAirMoisture());
//        assertEquals(4, skyTile3.getAirMoisture());
//        assertEquals(1, skyTile4.getAirMoisture());
//
//        skyTileList.parallelStream().forEach(SkyTile::moveClouds);
//        skyTileList.parallelStream().forEach(SkyTile::processIncommingMoisture);
//
//        assertEquals(1, skyTile1.getAirMoisture());
//        assertEquals(2, skyTile2.getAirMoisture());
//        assertEquals(3, skyTile3.getAirMoisture());
//        assertEquals(4, skyTile4.getAirMoisture());
    }
}