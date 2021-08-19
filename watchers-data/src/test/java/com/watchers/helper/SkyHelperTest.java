package com.watchers.helper;

import com.watchers.TestableWorld;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.OutgoingAircurrent;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class SkyHelperTest {

    @Test
    void assertAssertionsTest(){
        World world = TestableWorld.createWorld();
        SkyHelper.calculateAirflows(world);

        boolean allSkytilesHaveTwoIncommingAircurrents = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .map(SkyTile::getIncommingAircurrents)
                .allMatch(incommingAircurrent -> incommingAircurrent.size() == 2);

        Long outgoingAircurrents = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .map(SkyTile::getRawOutgoingAircurrents)
                .mapToLong(outgoingAircurrent -> outgoingAircurrent.getOutgoingAircurrent().size())
                .sum();

        Long incommingAircurrents = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .map(SkyTile::getRawIncommingAircurrents)
                .mapToLong(incommingAircurrent -> incommingAircurrent.getIncommingAircurrents().size())
                .sum();

        assertThat(allSkytilesHaveTwoIncommingAircurrents, equalTo(true));
        assertThat(outgoingAircurrents, equalTo(incommingAircurrents));
    }

    @Test
    void calculateFlowTest() {
        World world = new World();
        WorldSettings worldSettings = TestableWorld.createWorldSettings();
        world.setWorldSettings(worldSettings);
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

        List<Coordinate> coordinates = new ArrayList<>(world.getCoordinates());
        Comparator<Coordinate> xComparator = Comparator.comparing(Coordinate::getXCoord);
        Comparator<Coordinate> yComparator = Comparator.comparing(Coordinate::getYCoord);
        Comparator<Coordinate> coordinateComparator = yComparator.thenComparing(xComparator);
        coordinates.sort(coordinateComparator);
        for (long i = 0; i < world.getCoordinates().size(); i++) {
            coordinates.get((int) i).getClimate().getSkyTile().setId(i + 1);
            coordinates.get((int) i).getClimate().setId(i + 1);
            coordinates.get((int) i).setId(i + 1);
        }

        SkyHelper.calculateAirflows(world);

        sky1.getOutgoingLatitudallAirflow().setId(1L);
        sky2.getOutgoingLatitudallAirflow().setId(2L);
        sky3.getOutgoingLatitudallAirflow().setId(3L);
        sky4.getOutgoingLatitudallAirflow().setId(4L);
        sky5.getOutgoingLatitudallAirflow().setId(5L);

        sky6.getOutgoingLatitudallAirflow().setId(6L);
        sky7.getOutgoingLatitudallAirflow().setId(7L);
        sky8.getOutgoingLatitudallAirflow().setId(8L);
        sky9.getOutgoingLatitudallAirflow().setId(9L);
        sky10.getOutgoingLatitudallAirflow().setId(10L);

        sky11.getOutgoingLatitudallAirflow().setId(11L);
        sky12.getOutgoingLatitudallAirflow().setId(12L);
        sky13.getOutgoingLatitudallAirflow().setId(13L);
        sky14.getOutgoingLatitudallAirflow().setId(14L);
        sky15.getOutgoingLatitudallAirflow().setId(15L);

        sky16.getOutgoingLatitudallAirflow().setId(16L);
        sky17.getOutgoingLatitudallAirflow().setId(17L);
        sky18.getOutgoingLatitudallAirflow().setId(18L);
        sky19.getOutgoingLatitudallAirflow().setId(19L);

        sky20.getOutgoingLatitudallAirflow().setId(20L);
        sky21.getOutgoingLatitudallAirflow().setId(21L);
        sky22.getOutgoingLatitudallAirflow().setId(22L);
        sky23.getOutgoingLatitudallAirflow().setId(23L);
        sky24.getOutgoingLatitudallAirflow().setId(24L);
        sky25.getOutgoingLatitudallAirflow().setId(25L);

        sky26.getOutgoingLatitudallAirflow().setId(26L);
        sky27.getOutgoingLatitudallAirflow().setId(27L);
        sky28.getOutgoingLatitudallAirflow().setId(28L);
        sky29.getOutgoingLatitudallAirflow().setId(29L);
        sky30.getOutgoingLatitudallAirflow().setId(30L);


        sky1.getOutgoingLongitudalAirflow().setId(31L);
        sky2.getOutgoingLongitudalAirflow().setId(32L);
        sky3.getOutgoingLongitudalAirflow().setId(33L);
        sky4.getOutgoingLongitudalAirflow().setId(34L);
        sky5.getOutgoingLongitudalAirflow().setId(35L);

        sky6.getOutgoingLongitudalAirflow().setId(36L);
        sky7.getOutgoingLongitudalAirflow().setId(37L);
        sky8.getOutgoingLongitudalAirflow().setId(38L);
        sky9.getOutgoingLongitudalAirflow().setId(39L);
        sky10.getOutgoingLongitudalAirflow().setId(40L);

        sky11.getOutgoingLongitudalAirflow().setId(41L);
        sky12.getOutgoingLongitudalAirflow().setId(42L);
        sky13.getOutgoingLongitudalAirflow().setId(43L);
        sky14.getOutgoingLongitudalAirflow().setId(44L);
        sky15.getOutgoingLongitudalAirflow().setId(45L);

        sky16.getOutgoingLongitudalAirflow().setId(46L);
        sky17.getOutgoingLongitudalAirflow().setId(47L);
        sky18.getOutgoingLongitudalAirflow().setId(48L);
        sky19.getOutgoingLongitudalAirflow().setId(49L);

        sky20.getOutgoingLongitudalAirflow().setId(50L);
        sky21.getOutgoingLongitudalAirflow().setId(51L);
        sky22.getOutgoingLongitudalAirflow().setId(52L);
        sky23.getOutgoingLongitudalAirflow().setId(53L);
        sky24.getOutgoingLongitudalAirflow().setId(54L);
        sky25.getOutgoingLongitudalAirflow().setId(55L);

        sky26.getOutgoingLongitudalAirflow().setId(56L);
        sky27.getOutgoingLongitudalAirflow().setId(57L);
        sky28.getOutgoingLongitudalAirflow().setId(58L);
        sky29.getOutgoingLongitudalAirflow().setId(59L);
        sky30.getOutgoingLongitudalAirflow().setId(60L);

        long aircurrentCount = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .mapToLong(skyTile -> skyTile.getOutgoingAircurrents().size())
                .sum();

        assertEquals(60, aircurrentCount);

        // leftward -1
        assertEquals(1L, sky2.getIncommingLatitudalAirflow().getId());
        assertEquals(2L, sky3.getIncommingLatitudalAirflow().getId());
        assertEquals(3L, sky4.getIncommingLatitudalAirflow().getId());
        assertEquals(4L, sky5.getIncommingLatitudalAirflow().getId());
        assertEquals(5L, sky1.getIncommingLatitudalAirflow().getId());

        // rightward +1
        assertEquals(6L, sky10.getIncommingLatitudalAirflow().getId());
        assertEquals(7L, sky6.getIncommingLatitudalAirflow().getId());
        assertEquals(8L, sky7.getIncommingLatitudalAirflow().getId());
        assertEquals(9L, sky8.getIncommingLatitudalAirflow().getId());
        assertEquals(10L, sky9.getIncommingLatitudalAirflow().getId());

        // leftward -1
        assertEquals(11L, sky12.getIncommingLatitudalAirflow().getId());
        assertEquals(12L, sky13.getIncommingLatitudalAirflow().getId());
        assertEquals(13L, sky14.getIncommingLatitudalAirflow().getId());
        assertEquals(14L, sky15.getIncommingLatitudalAirflow().getId());
        assertEquals(15L, sky11.getIncommingLatitudalAirflow().getId());

        // leftward -1
        assertEquals(19L, sky20.getIncommingLatitudalAirflow().getId());
        assertEquals(20L, sky16.getIncommingLatitudalAirflow().getId());
        assertEquals(16L, sky17.getIncommingLatitudalAirflow().getId());
        assertEquals(17L, sky18.getIncommingLatitudalAirflow().getId());
        assertEquals(18L, sky19.getIncommingLatitudalAirflow().getId());

        // rightward -1
        assertEquals(21L, sky25.getIncommingLatitudalAirflow().getId());
        assertEquals(22L, sky21.getIncommingLatitudalAirflow().getId());
        assertEquals(23L, sky22.getIncommingLatitudalAirflow().getId());
        assertEquals(24L, sky23.getIncommingLatitudalAirflow().getId());
        assertEquals(25L, sky24.getIncommingLatitudalAirflow().getId());

        // leftward +1
        assertEquals(26L, sky27.getIncommingLatitudalAirflow().getId());
        assertEquals(27L, sky28.getIncommingLatitudalAirflow().getId());
        assertEquals(28L, sky29.getIncommingLatitudalAirflow().getId());
        assertEquals(29L, sky30.getIncommingLatitudalAirflow().getId());
        assertEquals(30, sky26.getIncommingLatitudalAirflow().getId());

        // left-upward + 30 -1 +5
        assertEquals(36L, sky1.getIncommingLongitudalAirflow().getId());
        assertEquals(37L, sky2.getIncommingLongitudalAirflow().getId());
        assertEquals(38L, sky3.getIncommingLongitudalAirflow().getId());
        assertEquals(39L, sky4.getIncommingLongitudalAirflow().getId());
        assertEquals(40L, sky5.getIncommingLongitudalAirflow().getId());

        // right-downward +30 +1 -5
        assertEquals(31L, sky6.getIncommingLongitudalAirflow().getId());
        assertEquals(32L, sky7.getIncommingLongitudalAirflow().getId());
        assertEquals(33L, sky8.getIncommingLongitudalAirflow().getId());
        assertEquals(34L, sky9.getIncommingLongitudalAirflow().getId());
        assertEquals(35L, sky10.getIncommingLongitudalAirflow().getId());

        // left-upward +30 -1 +5
        assertEquals(46L, sky11.getIncommingLongitudalAirflow().getId());
        assertEquals(47L, sky12.getIncommingLongitudalAirflow().getId());
        assertEquals(48L, sky13.getIncommingLongitudalAirflow().getId());
        assertEquals(49L, sky14.getIncommingLongitudalAirflow().getId());
        assertEquals(50L, sky15.getIncommingLongitudalAirflow().getId());

        // left-downward +30 -1 -5
        assertEquals(41L, sky16.getIncommingLongitudalAirflow().getId());
        assertEquals(42L, sky17.getIncommingLongitudalAirflow().getId());
        assertEquals(43L, sky18.getIncommingLongitudalAirflow().getId());
        assertEquals(44L, sky19.getIncommingLongitudalAirflow().getId());
        assertEquals(50L, sky20.getOutgoingLongitudalAirflow().getId());

        // right-upward +30 +1 +5
        assertEquals(56L, sky21.getIncommingLongitudalAirflow().getId());
        assertEquals(57L, sky22.getIncommingLongitudalAirflow().getId());
        assertEquals(58L, sky23.getIncommingLongitudalAirflow().getId());
        assertEquals(59L, sky24.getIncommingLongitudalAirflow().getId());
        assertEquals(60L, sky25.getIncommingLongitudalAirflow().getId());

        // left-downward +30 -1 -5
        assertEquals(56L, sky26.getOutgoingLongitudalAirflow().getId());
        assertEquals(57L, sky27.getOutgoingLongitudalAirflow().getId());
        assertEquals(58L, sky28.getOutgoingLongitudalAirflow().getId());
        assertEquals(59L, sky29.getOutgoingLongitudalAirflow().getId());
        assertEquals(60L, sky30.getOutgoingLongitudalAirflow().getId());
    }

}