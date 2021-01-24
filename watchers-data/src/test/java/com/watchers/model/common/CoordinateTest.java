package com.watchers.model.common;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void testCalculation(){
        World world = new World(58, 28);

        Coordinate coordinate = CoordinateFactory.createCoordinate(30,10, world, new Continent(world, SurfaceType.OCEAN));
        Coordinate soughtCoordinate = CoordinateFactory.createCoordinate(30,9, world, new Continent(world, SurfaceType.COASTAL));
        world.getCoordinates().add(soughtCoordinate);

        Coordinate distantCoordinate = coordinate.calculateDistantCoordinate(0, -1);

        assertEquals(9, distantCoordinate.getYCoord());
    }

}