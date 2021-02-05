package com.watchers.model.world;

import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContinentTest {

    @Test
    void calculateMostConnectedNeighbouringContinent() {
        World world = new World();
        world.setYSize(5L);
        world.setXSize(3L);
        Continent continent1 = new Continent(world, SurfaceType.OCEAN);
        continent1.setId(1L);
        Continent continent2 = new Continent(world, SurfaceType.OCEAN);
        continent2.setId(2L);
        Continent continent3 = new Continent(world, SurfaceType.OCEAN);
        continent3.setId(3L);

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,1, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,2, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,3, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,4, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,5, world, continent1));

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(2,1, world, continent1));
        continent2.getCoordinates().add(CoordinateFactory.createCoordinate(2,2, world, continent2));
        continent2.getCoordinates().add(CoordinateFactory.createCoordinate(2,3, world, continent2));
        continent3.getCoordinates().add(CoordinateFactory.createCoordinate(2,4, world, continent3));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(2,5, world, continent1));

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,1, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,2, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,3, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,4, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,5, world, continent1));

        world.getCoordinates().addAll(continent1.getCoordinates());
        world.getCoordinates().addAll(continent2.getCoordinates());
        world.getCoordinates().addAll(continent3.getCoordinates());

        Assertions.assertEquals(2L, continent1.calculateMostConnectedNeighbouringContinent());
        Assertions.assertEquals(1L, continent2.calculateMostConnectedNeighbouringContinent());
        Assertions.assertEquals(1L, continent3.calculateMostConnectedNeighbouringContinent());
    }
}