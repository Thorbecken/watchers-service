package com.watchers.model.world;

import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ContinentTest {

    World world;
    Continent continent;

    @BeforeEach
    void setUp() {
        world = new World(1,1);
        world.setWorldSettings(new WorldSettings());
        world.getWorldSettings().setLifePreSeeded(true);
        world.setLastContinentInFlux(99L);
        continent = new Continent(world, SurfaceType.COASTAL);
        continent.setId(1L);

        Direction direction = new Direction(1,1);
        continent.setDirection(direction);
    }

    @ParameterizedTest
    @CsvSource({"1", "2", "3"})
    void assignNewDriftDirection(int driftVelocity) {
        continent.assignNewDriftDirection(driftVelocity, world);

        assertThat("drift out of bound!", (driftVelocity*driftVelocity)>= continent.getDirection().getXVelocity()*continent.getDirection().getXVelocity(), is(true));
        assertThat("drift out of bound!", (driftVelocity*driftVelocity)>= continent.getDirection().getYVelocity()*continent.getDirection().getYVelocity(), is(true));

        assertThat(continent.getId(), equalTo(world.getLastContinentInFlux()));

    }

    @Test
    void calculateMostConnectedNeighbouringContinent() {
        World world = new World();
        world.setWorldSettings(new WorldSettings());
        world.getWorldSettings().setLifePreSeeded(true);
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
        Assertions.assertEquals(3L, continent2.calculateMostConnectedNeighbouringContinent());
        Assertions.assertEquals(2L, continent3.calculateMostConnectedNeighbouringContinent());
    }
}