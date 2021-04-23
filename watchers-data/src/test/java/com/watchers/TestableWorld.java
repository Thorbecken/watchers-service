package com.watchers;

import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.world.*;
import com.watchers.model.enums.SurfaceType;

import java.util.*;

public class TestableWorld {

    public static World createWorld() {
        return createWorld(WorldTypeEnum.NON_EUCLIDEAN);
    }

    public static World createWorld(WorldTypeEnum worldTypeEnum) {
        World world = new World(3, 3);
        world.setId(1L);
        world.setLastContinentInFlux(0L);

        WorldSettings worldSettings = TestableWorld.createWorldSettings();
        worldSettings.setHeigtDivider(2);
        worldSettings.setMinimumContinents(5);
        world.setWorldSettings(worldSettings);

        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setWorldTypeEnum(worldTypeEnum);

        Continent continent1 = new Continent(world, SurfaceType.PLAIN);
        continent1.setId(0L);
        continent1.setDirection(new Direction(1, 0));
        continent1.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(1, 1, world, continent1),
                CoordinateFactory.createCoordinate(1, 2, world, continent1),
                CoordinateFactory.createCoordinate(2, 1, world, continent1)
        ));

        Continent continent2 = new Continent(world, SurfaceType.COASTAL);
        continent2.setId(1L);
        continent2.setDirection(new Direction(0, -1));
        continent2.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(2, 2, world, continent2),
                CoordinateFactory.createCoordinate(3, 2, world, continent2),
                CoordinateFactory.createCoordinate(1, 3, world, continent2)
        ));

        Continent continent3 = new Continent(world, SurfaceType.OCEAN);
        continent3.setId(2L);
        continent3.setDirection(new Direction(0, 0));
        continent3.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(3, 3, world, continent3),
                CoordinateFactory.createCoordinate(3, 1, world, continent3),
                CoordinateFactory.createCoordinate(2, 3, world, continent3)
        ));

        world.setContinents(new HashSet<>(Arrays.asList(continent1, continent2, continent3)));
        world.setCoordinates(new HashSet<>());
        world.getCoordinates().addAll(continent1.getCoordinates());
        world.getCoordinates().addAll(continent2.getCoordinates());
        world.getCoordinates().addAll(continent3.getCoordinates());

        world.getCoordinates().forEach(coordinate -> {
                    coordinate.setWorld(coordinate.getWorld());
                    if (coordinate.getTile().getSurfaceType() == SurfaceType.PLAIN) {
                        coordinate.getTile().setHeight(40);
                    } else {
                        coordinate.getTile().setHeight(20);
                    }
                }
        );


        return world;
    }

    public static WorldSettings createWorldSettings(){
        return new WorldSettings(
                null,
                null,

                12,
                6,
                3,
                true,
                2,
                5,

                1,
                1,
                2,
                2,
                6,
                4,
                9000,
                2,
                3,
                5,
                10,

                // Climate settings
                60,
                40,
                20,
                0,

                10,
                5,
                2,
                1,
                0,

                7,
                1);
    }
}
