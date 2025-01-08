package com.watchers.model.climate;

import com.watchers.TestableWorld;
import com.watchers.helper.ClimateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static com.watchers.model.climate.Climate.DIURNAL_TEMPERATURE_CHANGE_PER_DEGREE_OF_LATITUDE;
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
        World world = new World(1, 1);
        world.setWorldSettings(new WorldSettings());
        world.getWorldSettings().setLifePreSeeded(true);
        Continent continent = new Continent(world, SurfaceType.OCEAN);
        Coordinate coordinate = CoordinateFactory.createCoordinate(1, 1, world, continent);
        Climate climate = coordinate.getClimate();

        assertTrue(climate.isWater());
        assertFalse(climate.isLand());
    }

    @Test
    void assertionsLand() {
        World world = new World(1, 1);
        world.setWorldSettings(new WorldSettings());
        world.getWorldSettings().setLifePreSeeded(true);
        Continent continent = new Continent(world, SurfaceType.PLAIN);
        Coordinate coordinate = CoordinateFactory.createCoordinate(1, 1, world, continent);
        Climate climate = coordinate.getClimate();

        assertTrue(climate.isLand());
        assertFalse(climate.isWater());
    }

    @Test
    void moveCloudsSimple() {
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
            skyTile.setMaximalAirMoisture(100);
            skyTile.addAirMoisture(2);
            skyTile.getOutgoingAircurrents().forEach(aircurrent -> aircurrent.setCurrentStrength(1));
        });

        assertEquals(2, sky1.getAirMoisture());
        assertEquals(2, sky2.getAirMoisture());
        assertEquals(2, sky3.getAirMoisture());
        assertEquals(2, sky4.getAirMoisture());

        skyTileList.parallelStream().forEach(Climate::moveClouds);
        skyTileList.parallelStream().forEach(Climate::processIncomingMoisture);

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

        skyTileList.forEach(skyTile -> {
            assertEquals(2, skyTile.getOutgoingAircurrents().size());
            skyTile.setMaximalAirMoisture(100);
        });
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
        skyTileList.parallelStream().forEach(Climate::processIncomingMoisture);

        double totalAirmoistureAfter = skyTileList.stream()
                .mapToDouble(Climate::getAirMoisture)
                .sum();

        assertThat(totalAirmoistureAfter, is(totalAirmoistureBefore));
    }

    @ParameterizedTest
    @CsvSource({
            "-25, 0.64",
            "-20, 1.05",
            "-15, 1.58",
            "-10, 2.31",
            "-5, 3.37",
            "0, 4.89",
            "5, 6.82",
            "10, 9.39",
            "15, 12.8",
            "20, 17.3",
            "30, 30.4",
            "40, 51.1",
            "50, 83.0",
            "60, 130"
    })
    public void testCalculateMaximumGramsOfWaterVapor(double temperature, double expected) {
        double result = new Climate().calculateMaximumGramsOfWaterVaporPerCubicMeter(temperature);
        assertEquals(expected, result, ((expected * 0.05) + 0.5));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0.6113",
            "20, 2.3388",
            "35, 5.6267",
            "50, 12.344",
            "75, 38.563",
            "100, 101.32"
    })
    public void testCalculateSaturatedVaporPressure(double temperature, double expectedPressure) {
        double calculatedPressure = new Climate().calculateSaturatedVaporPressure(temperature);
        assertEquals(expectedPressure, calculatedPressure, ((expectedPressure * 0.05) + 0.5));
    }

    @Test
    void testResetTemperatures(){
        World world = TestableWorld.createWorld();
        Climate climate = world.getCoordinate(2,2).getClimate();
        climate.setMeanTemperature(10d);
        climate.setAdiabaticTemperatureAdjustment(-2d);

        climate.resetTemperature();

        assertEquals(12d, climate.getMeanTemperature());

        climate.setAdiabaticTemperatureAdjustment(2d);
        climate.resetTemperature();

        assertEquals(10d, climate.getMeanTemperature());
    }

    @Test
    void testCalculateAdjustedTemperatureForAltitude(){
        World world = TestableWorld.createWorld();
        Climate climate = world.getCoordinate(2,2).getClimate();
        climate.getCoordinate().getTile().setHeight(4000);
        long seaLevel = 3000;

        climate.calculateAdjustedTemperatureForAltitude(seaLevel);

        assertEquals(-6.5d, climate.getAdiabaticTemperatureAdjustment());

        seaLevel = 4000;

        climate.calculateAdjustedTemperatureForAltitude(seaLevel);

        assertEquals(-0d, climate.getAdiabaticTemperatureAdjustment());

        seaLevel = 5000;

        climate.calculateAdjustedTemperatureForAltitude(seaLevel);

        assertEquals(-0d, climate.getAdiabaticTemperatureAdjustment());
    }

    @Test
    void testCalculateIncomingHeathFromAirCurrents(){
        World world = TestableWorld.createWorld();
        ClimateHelper.calculateAndWeaveAirflows(world);

        Climate climate = world.getCoordinate(2,2).getClimate();
        Aircurrent latitudeAirflow = climate.getIncomingLatitudeAirflow();
        Aircurrent longitudeAirflow = climate.getIncomingLongitudeAirflow();
        Climate latitudeClimate = latitudeAirflow.getStartingClimate();
        Climate longitudeClimate = longitudeAirflow.getStartingClimate();

        assertEquals(climate, climate.getIncomingLatitudeAirflow().getEndingClimate());
        assertEquals(climate, climate.getIncomingLongitudeAirflow().getEndingClimate());

        longitudeAirflow.setCurrentStrength(10);
        latitudeAirflow.setCurrentStrength(10);
        latitudeClimate.setMeanTemperature(10d);
        longitudeClimate.setMeanTemperature(10d);

        assertEquals(10d, climate.calculateIncomingHeathFromAirCurrents());

        latitudeClimate.setMeanTemperature(12d);
        longitudeClimate.setMeanTemperature(10d);

        assertEquals(11d, climate.calculateIncomingHeathFromAirCurrents());

        latitudeClimate.setMeanTemperature(10d);
        longitudeClimate.setMeanTemperature(12d);

        assertEquals(11d, climate.calculateIncomingHeathFromAirCurrents());

        latitudeClimate.setMeanTemperature(12d);
        longitudeClimate.setMeanTemperature(12d);

        assertEquals(12d, climate.calculateIncomingHeathFromAirCurrents());

        longitudeAirflow.setCurrentStrength(20);
        latitudeClimate.setMeanTemperature(40d);
        longitudeClimate.setMeanTemperature(10d);

        assertEquals(20d, climate.calculateIncomingHeathFromAirCurrents());

        longitudeAirflow.setCurrentStrength(10);
        latitudeAirflow.setCurrentStrength(20);
        latitudeClimate.setMeanTemperature(10d);
        longitudeClimate.setMeanTemperature(40d);

        assertEquals(20d, climate.calculateIncomingHeathFromAirCurrents());
    }

    @Test
    void testCalculateIncomingHeathFromWaterCurrents(){
        World world = TestableWorld.createWorld();

        Climate middleClimate = world.getCoordinate(2,2).getClimate();
        Climate upperClimate = world.getCoordinate(2,1).getClimate();
        Climate leftClimate = world.getCoordinate(1,2).getClimate();
        Climate rightClimate = world.getCoordinate(3,2).getClimate();
        Climate lowerClimate = world.getCoordinate(2,3).getClimate();

        world.getCoordinates().forEach(coordinate -> {
            coordinate.getClimate().setMeanTemperature(10d);
            coordinate.getTile().setSurfaceType(SurfaceType.PLAIN);
        });

        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isEmpty());

        world.getCoordinates().forEach(coordinate -> {
            coordinate.getClimate().setMeanTemperature(10d);
            coordinate.getTile().setSurfaceType(SurfaceType.SEA);
        });

        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isPresent());
        assertEquals(10d, middleClimate.calculateIncomingHeathFromWaterCurrents().get());

        leftClimate.setMeanTemperature(50d);

        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isPresent());
        assertEquals(20d, middleClimate.calculateIncomingHeathFromWaterCurrents().get());

        leftClimate.getCoordinate().getTile().setSurfaceType(SurfaceType.MOUNTAIN);

        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isPresent());
        assertEquals(10d, middleClimate.calculateIncomingHeathFromWaterCurrents().get());

        rightClimate.getCoordinate().getTile().setSurfaceType(SurfaceType.MOUNTAIN);
        upperClimate.getCoordinate().getTile().setSurfaceType(SurfaceType.MOUNTAIN);

        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isPresent());
        assertEquals(10d, middleClimate.calculateIncomingHeathFromWaterCurrents().get());

        lowerClimate.getCoordinate().getTile().setSurfaceType(SurfaceType.MOUNTAIN);

        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isEmpty());
    }

    @Test
    void testCalculateAndSetTemporaryMeanTemperature(){
        World world = TestableWorld.createWorld();
        ClimateHelper.calculateAndWeaveAirflows(world);

        Climate middleClimate = world.getCoordinate(2,2).getClimate();
        Climate upperClimate = world.getCoordinate(2,1).getClimate();
        Climate leftClimate = world.getCoordinate(1,2).getClimate();
        Climate rightClimate = world.getCoordinate(3,2).getClimate();
        Climate lowerClimate = world.getCoordinate(2,3).getClimate();

        world.getCoordinates().forEach(coordinate -> {
            coordinate.getClimate().setMeanTemperature(10d);
            coordinate.getClimate().setSolarTemperature(10d);
            coordinate.getTile().setSurfaceType(SurfaceType.PLAIN);
        });

        middleClimate.calculateAndSetTemporaryMeanTemperature();
        assertEquals(10d, middleClimate.getTemporaryMeanTemperature());

        Aircurrent latitudeAirflow = middleClimate.getIncomingLatitudeAirflow();
        Aircurrent longitudeAirflow = middleClimate.getIncomingLongitudeAirflow();
        Climate latitudeClimate = latitudeAirflow.getStartingClimate();
        Climate longitudeClimate = longitudeAirflow.getStartingClimate();

        assertEquals(middleClimate, middleClimate.getIncomingLatitudeAirflow().getEndingClimate());
        assertEquals(middleClimate, middleClimate.getIncomingLatitudeAirflow().getEndingClimate());
        assertEquals(leftClimate, middleClimate.getIncomingLongitudeAirflow().getEndingClimate());
        assertEquals(upperClimate, middleClimate.getIncomingLongitudeAirflow().getEndingClimate());

        longitudeAirflow.setCurrentStrength(10);
        latitudeAirflow.setCurrentStrength(10);

        latitudeClimate.setMeanTemperature(30d);
        longitudeClimate.setMeanTemperature(20d);

        assertEquals(25d, middleClimate.calculateIncomingHeathFromAirCurrents());

        middleClimate.calculateAndSetTemporaryMeanTemperature();
        assertEquals(17.5d, middleClimate.getTemporaryMeanTemperature());

        ///////////////////////////////////////////

        world.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.setSurfaceType(SurfaceType.SEA));
        middleClimate.getCoordinate().getTile().setSurfaceType(SurfaceType.MOUNTAIN);

        middleClimate.calculateAndSetTemporaryMeanTemperature();
        assertEquals(17.5d, middleClimate.getTemporaryMeanTemperature());

        ///////////////////////////////////////////

        middleClimate.getCoordinate().getTile().setSurfaceType(SurfaceType.SEA);
        middleClimate.calculateAndSetTemporaryMeanTemperature();
        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isPresent());
        assertEquals(17.5d, middleClimate.calculateIncomingHeathFromWaterCurrents().get());
        assertEquals(17.5d, middleClimate.getTemporaryMeanTemperature());

        ///////////////////////////////////////////

        rightClimate.setMeanTemperature(30);
        lowerClimate.setMeanTemperature(30);
        assertTrue(middleClimate.calculateIncomingHeathFromWaterCurrents().isPresent());
        assertEquals(22.5d, middleClimate.calculateIncomingHeathFromWaterCurrents().get());

        middleClimate.calculateAndSetTemporaryMeanTemperature();
        assertEquals(19.2d, middleClimate.getTemporaryMeanTemperature(), 0.1d);
    }

    @Test
    void testProcessRainfallAndCondensation(){
        World world = TestableWorld.createWorld();
        Climate climate = world.getCoordinate(2,2).getClimate();
        Tile tile = world.getCoordinate(2,2).getTile();

        climate.setAirMoisture(0d);
        tile.setRainfall(0d);

        climate.processRainfallAndCondensation();

        assertEquals(0d, climate.getAirMoisture());
        assertEquals(0d, tile.getRainfall());

        ///////////////////////////////////////////

        climate.setAirMoisture(10d);
        climate.setMaximalAirMoistureNight(10d);
        climate.setMaximalAirMoisture(10d);
        tile.setRainfall(0d);

        climate.processRainfallAndCondensation();

        assertEquals(10d, climate.getAirMoisture());
        assertEquals(0d, tile.getRainfall());

        ///////////////////////////////////////////

        climate.setAirMoisture(10d);
        climate.setMaximalAirMoistureNight(10d);
        climate.setMaximalAirMoisture(20d);
        tile.setRainfall(0d);

        climate.processRainfallAndCondensation();

        assertEquals(10d, climate.getAirMoisture());
        assertEquals(0d, tile.getRainfall());

        ///////////////////////////////////////////

        climate.setAirMoisture(20d);
        climate.setMaximalAirMoistureNight(10d);
        climate.setMaximalAirMoisture(20d);
        tile.setRainfall(0d);

        climate.processRainfallAndCondensation();

        assertEquals(10d, climate.getAirMoisture());
        assertEquals(10d, tile.getRainfall());

        ///////////////////////////////////////////

        climate.setAirMoisture(20d);
        climate.setMaximalAirMoistureNight(20d);
        climate.setMaximalAirMoisture(10d);
        tile.setRainfall(0d);

        climate.processRainfallAndCondensation();

        assertEquals(10d, climate.getAirMoisture());
        assertEquals(10d, tile.getRainfall());

        ///////////////////////////////////////////

        climate.setAirMoisture(30d);
        climate.setMaximalAirMoistureNight(10d);
        climate.setMaximalAirMoisture(20d);
        tile.setRainfall(0d);

        climate.processRainfallAndCondensation();

        assertEquals(10d, climate.getAirMoisture());
        assertEquals(20d, tile.getRainfall());
    }

    @Test
    void testAdjustTemperatureAndMoistureLevel(){
        World world = TestableWorld.createWorld();
        double airMoisture = 100;
        Tile middleTile = world.getCoordinate(2,2).getTile();
        middleTile.setRainfall(0d);
        Climate middleClimate = world.getCoordinate(2,2).getClimate();
        middleClimate.setMeanTemperature(10d);
        middleClimate.setMaximalAirMoisture(10d);
        middleClimate.setAirMoisture(airMoisture);
        middleClimate.setTemporaryMeanTemperature(30d);
        middleClimate.setAdiabaticTemperatureAdjustment(-10d);

        double temperatureSwing = DIURNAL_TEMPERATURE_CHANGE_PER_DEGREE_OF_LATITUDE * Math.abs(middleClimate.getLatitude());
        double meanTemperature = 20d;
        double dayTemperature = meanTemperature + temperatureSwing;
        double nightTemperature = meanTemperature - temperatureSwing;

        double expectedMaximalAirMoisture = middleClimate.calculateMaximumGramsOfWaterVaporPerCubicMeter(meanTemperature);
        double expectedMaximalAirMoistureNight = middleClimate.calculateMaximumGramsOfWaterVaporPerCubicMeter(nightTemperature);
        double expectedMaximalAirMoistureDay = middleClimate.calculateMaximumGramsOfWaterVaporPerCubicMeter(dayTemperature);

        middleClimate.adjustTemperatureAndMoistureLevel();

        assertEquals(meanTemperature, middleClimate.getMeanTemperature());
        assertEquals(dayTemperature, middleClimate.getDayTemperature());
        assertEquals(nightTemperature, middleClimate.getNightTemperature());

        assertEquals(expectedMaximalAirMoisture, middleClimate.getMaximalAirMoisture());
        assertEquals(expectedMaximalAirMoistureNight, middleClimate.getMaximalAirMoistureNight());
        assertEquals(expectedMaximalAirMoistureDay, middleClimate.getMaximalAirMoistureDay());

        assertEquals(airMoisture - expectedMaximalAirMoistureNight, middleClimate.getCoordinate().getTile().getRainfall());
        assertEquals(expectedMaximalAirMoistureNight, middleClimate.getAirMoisture());
    }

    @Test
    void testRecalculateTemperatures(){
        World world = TestableWorld.createWorld();
        ClimateHelper.calculateAndWeaveAirflows(world);

        Climate middleClimate = world.getCoordinate(2,2).getClimate();

        world.getCoordinates().forEach(coordinate -> {
            coordinate.getClimate().setMeanTemperature(20d);
            coordinate.getTile().setSurfaceType(SurfaceType.PLAIN);
        });

        middleClimate.setSolarTemperature(10d);

        Climate.recalculateTemperatures(world);

        assertEquals(15d, middleClimate.getMeanTemperature());
    }
}