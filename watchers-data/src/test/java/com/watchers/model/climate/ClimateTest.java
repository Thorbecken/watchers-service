package com.watchers.model.climate;

import com.watchers.TestableWorld;
import com.watchers.helper.ClimateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class ClimateTest {

    private static final long LOW_HEIGHT = 2;
    private static final long MEDIUM_HEIGHT = 4;
    private static final long HIGH_HEIGHT = 8;
    private static final long NO_HEIGHT = 0;

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

    @Test
    void moveCloudsSimmple() {
        World world = new World();
        world.setWorldSettings(TestableWorld.createWorldSettings());
        world.setXSize(2L);
        world.setYSize(2L);
        Continent continent = new Continent(world, SurfaceType.OCEAN);

        Climate sky1 = CoordinateFactory.createCoordinate(1, 1, world, continent).getClimate();
        Climate sky2 = CoordinateFactory.createCoordinate(2, 1, world, continent).getClimate();
        Climate sky3 = CoordinateFactory.createCoordinate(1, 2, world, continent).getClimate();
        Climate sky4 = CoordinateFactory.createCoordinate(2, 2, world, continent).getClimate();
        ClimateHelper.calculateAndWeaveAirflows(world);

        List<Climate> skyTileList = Arrays.asList(
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

        skyTileList.parallelStream().forEach(Climate::moveClouds);
        skyTileList.parallelStream().forEach(Climate::processIncommingMoisture);

        assertEquals(2, sky1.getAirMoisture());
        assertEquals(2, sky2.getAirMoisture());
        assertEquals(2, sky3.getAirMoisture());
        assertEquals(2, sky4.getAirMoisture());
    }

    @Test
    void moveClouds() {
        World world = new World();
        world.setWorldSettings(TestableWorld.createWorldSettings());
        world.setXSize(5L);
        world.setYSize(6L);
        Continent continent = new Continent(world, SurfaceType.OCEAN);

        Climate sky1 = CoordinateFactory.createCoordinate(1, 1, world, continent).getClimate();
        Climate sky2 = CoordinateFactory.createCoordinate(2, 1, world, continent).getClimate();
        Climate sky3 = CoordinateFactory.createCoordinate(3, 1, world, continent).getClimate();
        Climate sky4 = CoordinateFactory.createCoordinate(4, 1, world, continent).getClimate();
        Climate sky5 = CoordinateFactory.createCoordinate(5, 1, world, continent).getClimate();

        Climate sky6 = CoordinateFactory.createCoordinate(1, 2, world, continent).getClimate();
        Climate sky7 = CoordinateFactory.createCoordinate(2, 2, world, continent).getClimate();
        Climate sky8 = CoordinateFactory.createCoordinate(3, 2, world, continent).getClimate();
        Climate sky9 = CoordinateFactory.createCoordinate(4, 2, world, continent).getClimate();
        Climate sky10 = CoordinateFactory.createCoordinate(5, 2, world, continent).getClimate();

        Climate sky11 = CoordinateFactory.createCoordinate(1, 3, world, continent).getClimate();
        Climate sky12 = CoordinateFactory.createCoordinate(2, 3, world, continent).getClimate();
        Climate sky13 = CoordinateFactory.createCoordinate(3, 3, world, continent).getClimate();
        Climate sky14 = CoordinateFactory.createCoordinate(4, 3, world, continent).getClimate();
        Climate sky15 = CoordinateFactory.createCoordinate(5, 3, world, continent).getClimate();

        Climate sky16 = CoordinateFactory.createCoordinate(1, 4, world, continent).getClimate();
        Climate sky17 = CoordinateFactory.createCoordinate(2, 4, world, continent).getClimate();
        Climate sky18 = CoordinateFactory.createCoordinate(3, 4, world, continent).getClimate();
        Climate sky19 = CoordinateFactory.createCoordinate(4, 4, world, continent).getClimate();
        Climate sky20 = CoordinateFactory.createCoordinate(5, 4, world, continent).getClimate();

        Climate sky21 = CoordinateFactory.createCoordinate(1, 5, world, continent).getClimate();
        Climate sky22 = CoordinateFactory.createCoordinate(2, 5, world, continent).getClimate();
        Climate sky23 = CoordinateFactory.createCoordinate(3, 5, world, continent).getClimate();
        Climate sky24 = CoordinateFactory.createCoordinate(4, 5, world, continent).getClimate();
        Climate sky25 = CoordinateFactory.createCoordinate(5, 5, world, continent).getClimate();

        Climate sky26 = CoordinateFactory.createCoordinate(1, 6, world, continent).getClimate();
        Climate sky27 = CoordinateFactory.createCoordinate(2, 6, world, continent).getClimate();
        Climate sky28 = CoordinateFactory.createCoordinate(3, 6, world, continent).getClimate();
        Climate sky29 = CoordinateFactory.createCoordinate(4, 6, world, continent).getClimate();
        Climate sky30 = CoordinateFactory.createCoordinate(5, 6, world, continent).getClimate();

        ClimateHelper.calculateAndWeaveAirflows(world);

        List<Climate> skyTileList = Arrays.asList(
                sky1, sky2, sky3, sky4, sky5,
                sky6, sky7, sky8, sky9, sky10,
                sky11, sky12, sky13, sky14, sky15,
                sky16, sky17, sky18, sky19, sky20,
                sky21, sky22, sky23, sky24, sky25,
                sky26, sky27, sky28, sky29, sky30
        );

        skyTileList.forEach(skyTile -> assertEquals(2, skyTile.getOutgoingAircurrents().size()));
        skyTileList.stream()
                .flatMap(skyTile -> skyTile.getOutgoingAircurrents().stream())
                .forEach(aircurrent -> aircurrent.setCurrentStrength(1));

        // preparation for height difference check
        sky1.getCoordinate().getTile().setHeight(LOW_HEIGHT);
        sky2.getCoordinate().getTile().setHeight(MEDIUM_HEIGHT);
        sky3.getCoordinate().getTile().setHeight(HIGH_HEIGHT);
        sky4.getCoordinate().getTile().setHeight(NO_HEIGHT);
        sky5.getCoordinate().getTile().setHeight(NO_HEIGHT);

        assertEquals(0, sky1.getAirMoisture());
        assertEquals(0, sky2.getAirMoisture());
        assertEquals(0, sky3.getAirMoisture());
        assertEquals(0, sky4.getAirMoisture());
        assertEquals(0, sky5.getAirMoisture());

        skyTileList.forEach(skyTile -> {
                    long xCoord = skyTile.getCoordinate().getXCoord();
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

        assertThat(sky1.getAirMoisture(), is(2.0)); // 6 -> 2  -4
        assertThat(sky2.getAirMoisture(), is(4.0)); // 3 -> 4  +1
        assertThat(sky3.getAirMoisture(), is(6.0)); // 5 -> 6  +1
        assertThat(sky4.getAirMoisture(), is(8.0)); // 7 -> 8  +1
        assertThat(sky5.getAirMoisture(), is(10.0)); // 9 -> 10 +1

        double totalAirmoistureBefore = skyTileList.stream()
                .mapToDouble(Climate::getAirMoisture)
                .sum();

        skyTileList.parallelStream().forEach(Climate::moveClouds);
        skyTileList.parallelStream().forEach(Climate::processIncommingMoisture);

        double totalAirmoistureAfter = skyTileList.stream()
                .mapToDouble(Climate::getAirMoisture)
                .sum();

        assertThat(totalAirmoistureAfter, is(totalAirmoistureBefore));
    }

}