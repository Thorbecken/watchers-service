package com.watchers.model.common;

import com.watchers.model.environment.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void testCalculation(){
        World world = new World(58, 28);

        Coordinate coordinate = new Coordinate(30,10, world);
        Coordinate distantCoordinate = coordinate.calculateDistantCoordinate(0, -1);

        assertEquals(9, distantCoordinate.getYCoord());
    }

}