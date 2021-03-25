package com.watchers.helper;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SkyHelperTest {

    @Test
    void calculateFlowTest(){
        World world = new World();
        world.setXSize(5L);
        world.setYSize(2L);
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

        SkyHelper.calculateAirflows(world);

        sky1.getOutgoingAircurrents().get(0).setId(1L);
        sky2.getOutgoingAircurrents().get(0).setId(2L);
        sky3.getOutgoingAircurrents().get(0).setId(3L);
        sky4.getOutgoingAircurrents().get(0).setId(4L);
        sky5.getOutgoingAircurrents().get(0).setId(5L);
        sky6.getOutgoingAircurrents().get(0).setId(6L);
        sky7.getOutgoingAircurrents().get(0).setId(7L);
        sky8.getOutgoingAircurrents().get(0).setId(8L);
        sky9.getOutgoingAircurrents().get(0).setId(9L);
        sky10.getOutgoingAircurrents().get(0).setId(10L);

        List<Aircurrent> aircurrentList = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .flatMap(skyTile -> skyTile.getOutgoingAircurrents().stream())
                .collect(Collectors.toList());

        assertEquals(10, aircurrentList.size());

        assertEquals(1L, sky1.getOutgoingAircurrents().get(0).getId());
        assertEquals(2L, sky2.getOutgoingAircurrents().get(0).getId());
        assertEquals(3L, sky3.getOutgoingAircurrents().get(0).getId());
        assertEquals(4L, sky4.getOutgoingAircurrents().get(0).getId());
        assertEquals(5L, sky5.getOutgoingAircurrents().get(0).getId());

        assertEquals(6L, sky6.getOutgoingAircurrents().get(0).getId());
        assertEquals(7L, sky7.getOutgoingAircurrents().get(0).getId());
        assertEquals(8L, sky8.getOutgoingAircurrents().get(0).getId());
        assertEquals(9L, sky9.getOutgoingAircurrents().get(0).getId());
        assertEquals(10L, sky10.getOutgoingAircurrents().get(0).getId());

        assertEquals(1L, (long) sky2.incommingAircurrents.get(0).getId());
        assertEquals(2L, (long) sky3.incommingAircurrents.get(0).getId());
        assertEquals(3L, (long) sky4.incommingAircurrents.get(0).getId());
        assertEquals(4L, (long) sky5.incommingAircurrents.get(0).getId());
        assertEquals(5L, (long) sky1.incommingAircurrents.get(0).getId());

        assertEquals(6L, (long) sky7.incommingAircurrents.get(0).getId());
        assertEquals(7L, (long) sky8.incommingAircurrents.get(0).getId());
        assertEquals(8L, (long) sky9.incommingAircurrents.get(0).getId());
        assertEquals(9L, (long) sky10.incommingAircurrents.get(0).getId());
        assertEquals(10L, (long) sky6.incommingAircurrents.get(0).getId());

        assertSame(sky1.incommingAircurrents.get(0).getId(), sky5.getOutgoingAircurrents().get(0).getId());
        assertSame(sky2.incommingAircurrents.get(0).getId(), sky1.getOutgoingAircurrents().get(0).getId());
        assertSame(sky3.incommingAircurrents.get(0).getId(), sky2.getOutgoingAircurrents().get(0).getId());
        assertSame(sky4.incommingAircurrents.get(0).getId(), sky3.getOutgoingAircurrents().get(0).getId());
        assertSame(sky5.incommingAircurrents.get(0).getId(), sky4.getOutgoingAircurrents().get(0).getId());

        assertSame(sky6.incommingAircurrents.get(0).getId(), sky10.getOutgoingAircurrents().get(0).getId());
        assertSame(sky7.incommingAircurrents.get(0).getId(), sky6.getOutgoingAircurrents().get(0).getId());
        assertSame(sky8.incommingAircurrents.get(0).getId(), sky7.getOutgoingAircurrents().get(0).getId());
        assertSame(sky9.incommingAircurrents.get(0).getId(), sky8.getOutgoingAircurrents().get(0).getId());
        assertSame(sky10.incommingAircurrents.get(0).getId(), sky9.getOutgoingAircurrents().get(0).getId());
    }

}