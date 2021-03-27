package com.watchers.helper;

import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SkyHelperTest {

    @Test
    void calculateFlowTest(){
        World world = new World();
        world.setXSize(5L);
        world.setYSize(6L);
        Continent continent = new Continent(world, SurfaceType.OCEAN);

        SkyTile sky1 = CoordinateFactory.createCoordinate(1,1, world, continent).getClimate().getSkyTile();
        SkyTile sky2 = CoordinateFactory.createCoordinate(2,1, world, continent).getClimate().getSkyTile();
        SkyTile sky3 = CoordinateFactory.createCoordinate(3,1, world, continent).getClimate().getSkyTile();
        SkyTile sky4 = CoordinateFactory.createCoordinate(4,1, world, continent).getClimate().getSkyTile();
        SkyTile sky5 = CoordinateFactory.createCoordinate(5,1, world, continent).getClimate().getSkyTile();

        SkyTile sky6 = CoordinateFactory.createCoordinate(1,2, world, continent).getClimate().getSkyTile();
        SkyTile sky7 = CoordinateFactory.createCoordinate(2,2, world, continent).getClimate().getSkyTile();
        SkyTile sky8 = CoordinateFactory.createCoordinate(3,2, world, continent).getClimate().getSkyTile();
        SkyTile sky9 = CoordinateFactory.createCoordinate(4,2, world, continent).getClimate().getSkyTile();
        SkyTile sky10 = CoordinateFactory.createCoordinate(5,2, world, continent).getClimate().getSkyTile();

        SkyTile sky11 = CoordinateFactory.createCoordinate(1,3, world, continent).getClimate().getSkyTile();
        SkyTile sky12 = CoordinateFactory.createCoordinate(2,3, world, continent).getClimate().getSkyTile();
        SkyTile sky13 = CoordinateFactory.createCoordinate(3,3, world, continent).getClimate().getSkyTile();
        SkyTile sky14 = CoordinateFactory.createCoordinate(4,3, world, continent).getClimate().getSkyTile();
        SkyTile sky15 = CoordinateFactory.createCoordinate(5,3, world, continent).getClimate().getSkyTile();

        SkyTile sky16 = CoordinateFactory.createCoordinate(1,4, world, continent).getClimate().getSkyTile();
        SkyTile sky17 = CoordinateFactory.createCoordinate(2,4, world, continent).getClimate().getSkyTile();
        SkyTile sky18 = CoordinateFactory.createCoordinate(3,4, world, continent).getClimate().getSkyTile();
        SkyTile sky19 = CoordinateFactory.createCoordinate(4,4, world, continent).getClimate().getSkyTile();
        SkyTile sky20 = CoordinateFactory.createCoordinate(5,4, world, continent).getClimate().getSkyTile();

        SkyTile sky21 = CoordinateFactory.createCoordinate(1,5, world, continent).getClimate().getSkyTile();
        SkyTile sky22 = CoordinateFactory.createCoordinate(2,5, world, continent).getClimate().getSkyTile();
        SkyTile sky23 = CoordinateFactory.createCoordinate(3,5, world, continent).getClimate().getSkyTile();
        SkyTile sky24 = CoordinateFactory.createCoordinate(4,5, world, continent).getClimate().getSkyTile();
        SkyTile sky25 = CoordinateFactory.createCoordinate(5,5, world, continent).getClimate().getSkyTile();

        SkyTile sky26 = CoordinateFactory.createCoordinate(1,6, world, continent).getClimate().getSkyTile();
        SkyTile sky27 = CoordinateFactory.createCoordinate(2,6, world, continent).getClimate().getSkyTile();
        SkyTile sky28 = CoordinateFactory.createCoordinate(3,6, world, continent).getClimate().getSkyTile();
        SkyTile sky29 = CoordinateFactory.createCoordinate(4,6, world, continent).getClimate().getSkyTile();
        SkyTile sky30 = CoordinateFactory.createCoordinate(5,6, world, continent).getClimate().getSkyTile();

        List<Coordinate> coordinates = new ArrayList<>(world.getCoordinates());
        Comparator<Coordinate> xComparator = Comparator.comparing(Coordinate::getXCoord);
        Comparator<Coordinate> yComparator = Comparator.comparing(Coordinate::getYCoord);
        Comparator<Coordinate> coordinateComparator = yComparator.thenComparing(xComparator);
        coordinates.sort(coordinateComparator);
        for (long i = 0; i < world.getCoordinates().size(); i++) {
            coordinates.get((int)i).getClimate().getSkyTile().setId(i+1);
            coordinates.get((int)i).getClimate().setId(i+1);
            coordinates.get((int)i).setId(i+1);
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

        // leftward
        assertEquals(1L, sky5.getIncommingLatitudalAirflow().getId());
        assertEquals(2L, sky1.getIncommingLatitudalAirflow().getId());
        assertEquals(3L, sky2.getIncommingLatitudalAirflow().getId());
        assertEquals(4L, sky3.getIncommingLatitudalAirflow().getId());
        assertEquals(5L, sky4.getIncommingLatitudalAirflow().getId());

        // rightward
        assertEquals(9L, sky10.getIncommingLatitudalAirflow().getId());
        assertEquals(10L, sky6.getIncommingLatitudalAirflow().getId());
        assertEquals(6L, sky7.getIncommingLatitudalAirflow().getId());
        assertEquals(7L, sky8.getIncommingLatitudalAirflow().getId());
        assertEquals(8L, sky9.getIncommingLatitudalAirflow().getId());

        // leftward
        assertEquals(11L, sky15.getIncommingLatitudalAirflow().getId());
        assertEquals(12L, sky11.getIncommingLatitudalAirflow().getId());
        assertEquals(13L, sky12.getIncommingLatitudalAirflow().getId());
        assertEquals(14L, sky13.getIncommingLatitudalAirflow().getId());
        assertEquals(15L, sky14.getIncommingLatitudalAirflow().getId());

        // leftward
        assertEquals(16L, sky20.getIncommingLatitudalAirflow().getId());
        assertEquals(17L, sky16.getIncommingLatitudalAirflow().getId());
        assertEquals(18L, sky17.getIncommingLatitudalAirflow().getId());
        assertEquals(19L, sky18.getIncommingLatitudalAirflow().getId());
        assertEquals(20L, sky19.getIncommingLatitudalAirflow().getId());

        // rightward
        assertEquals(24L, sky25.getIncommingLatitudalAirflow().getId());
        assertEquals(25L, sky21.getIncommingLatitudalAirflow().getId());
        assertEquals(21L, sky22.getIncommingLatitudalAirflow().getId());
        assertEquals(22L, sky23.getIncommingLatitudalAirflow().getId());
        assertEquals(23L, sky24.getIncommingLatitudalAirflow().getId());

        // leftward
        assertEquals(26L, sky30.getIncommingLatitudalAirflow().getId());
        assertEquals(27L, sky26.getIncommingLatitudalAirflow().getId());
        assertEquals(28L, sky27.getIncommingLatitudalAirflow().getId());
        assertEquals(29L, sky28.getIncommingLatitudalAirflow().getId());
        assertEquals(30L, sky29.getIncommingLatitudalAirflow().getId());

        // downward
        assertEquals(7L, sky1.getOutgoingLongitudalAirflow().getId());
        assertEquals(8L, sky2.getOutgoingLongitudalAirflow().getId());
        assertEquals(9L, sky3.getOutgoingLongitudalAirflow().getId());
        assertEquals(10L, sky4.getOutgoingLongitudalAirflow().getId());
        assertEquals(6L, sky5.getOutgoingLongitudalAirflow().getId());

        // upward
        assertEquals(6L, sky6.getOutgoingLongitudalAirflow().getId());
        assertEquals(7L, sky7.getOutgoingLongitudalAirflow().getId());
        assertEquals(8L, sky8.getOutgoingLongitudalAirflow().getId());
        assertEquals(9L, sky9.getOutgoingLongitudalAirflow().getId());
        assertEquals(10L, sky10.getOutgoingLongitudalAirflow().getId());

        // downward
        assertEquals(1L, sky11.getOutgoingLongitudalAirflow().getId());
        assertEquals(2L, sky12.getOutgoingLongitudalAirflow().getId());
        assertEquals(3L, sky13.getOutgoingLongitudalAirflow().getId());
        assertEquals(4L, sky14.getOutgoingLongitudalAirflow().getId());
        assertEquals(5L, sky15.getOutgoingLongitudalAirflow().getId());

        // upward
        assertEquals(6L, sky16.getOutgoingLongitudalAirflow().getId());
        assertEquals(7L, sky17.getOutgoingLongitudalAirflow().getId());
        assertEquals(8L, sky18.getOutgoingLongitudalAirflow().getId());
        assertEquals(9L, sky19.getOutgoingLongitudalAirflow().getId());
        assertEquals(10L, sky20.getOutgoingLongitudalAirflow().getId());

        // downward
        assertEquals(1L, sky21.getOutgoingLongitudalAirflow().getId());
        assertEquals(2L, sky22.getOutgoingLongitudalAirflow().getId());
        assertEquals(3L, sky23.getOutgoingLongitudalAirflow().getId());
        assertEquals(4L, sky24.getOutgoingLongitudalAirflow().getId());
        assertEquals(5L, sky25.getOutgoingLongitudalAirflow().getId());

        // upward
        assertEquals(6L, sky26.getOutgoingLongitudalAirflow().getId());
        assertEquals(7L, sky27.getOutgoingLongitudalAirflow().getId());
        assertEquals(8L, sky28.getOutgoingLongitudalAirflow().getId());
        assertEquals(9L, sky29.getOutgoingLongitudalAirflow().getId());
        assertEquals(10L, sky30.getOutgoingLongitudalAirflow().getId());

//        assertEquals(1L, (long) sky2.incommingAircurrents.get(0).getId());
//        assertEquals(2L, (long) sky3.incommingAircurrents.get(0).getId());
//        assertEquals(3L, (long) sky4.incommingAircurrents.get(0).getId());
//        assertEquals(4L, (long) sky5.incommingAircurrents.get(0).getId());
//        assertEquals(5L, (long) sky1.incommingAircurrents.get(0).getId());
//
//        assertEquals(6L, (long) sky7.incommingAircurrents.get(0).getId());
//        assertEquals(7L, (long) sky8.incommingAircurrents.get(0).getId());
//        assertEquals(8L, (long) sky9.incommingAircurrents.get(0).getId());
//        assertEquals(9L, (long) sky10.incommingAircurrents.get(0).getId());
//        assertEquals(10L, (long) sky6.incommingAircurrents.get(0).getId());
//
//        assertSame(sky1.incommingAircurrents.get(0).getId(), sky5.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky2.incommingAircurrents.get(0).getId(), sky1.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky3.incommingAircurrents.get(0).getId(), sky2.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky4.incommingAircurrents.get(0).getId(), sky3.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky5.incommingAircurrents.get(0).getId(), sky4.getOutgoingAircurrents().get(0).getId());
//
//        assertSame(sky6.incommingAircurrents.get(0).getId(), sky10.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky7.incommingAircurrents.get(0).getId(), sky6.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky8.incommingAircurrents.get(0).getId(), sky7.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky9.incommingAircurrents.get(0).getId(), sky8.getOutgoingAircurrents().get(0).getId());
//        assertSame(sky10.incommingAircurrents.get(0).getId(), sky9.getOutgoingAircurrents().get(0).getId());
    }

}