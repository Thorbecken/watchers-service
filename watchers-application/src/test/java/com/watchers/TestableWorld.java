package com.watchers;

import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.*;

import java.util.Arrays;
import java.util.HashSet;

public class TestableWorld {

    public static World createWorld() {
        return createWorld(WorldTypeEnum.NON_EUCLIDEAN);
    }

    public static World createWorld(WorldTypeEnum worldTypeEnum) {
        World world = new World(3, 3);
        world.setId(1L);
        world.setLastContinentInFlux(0L);

        WorldSettings worldSettings = TestableWorld.createWorldSettings();
        worldSettings.setId(1L);
        worldSettings.setHeigtDivider(2);
        worldSettings.setMinimumContinents(5);
        world.setWorldSettings(worldSettings);

        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setId(1L);
        worldMetaData.setWorldTypeEnum(worldTypeEnum);
        worldMetaData.setWorld(world);
        worldMetaData.setXSize(world.getXSize());
        worldMetaData.setYSize(world.getYSize());
        worldMetaData.setId(world.getId());
        world.setWorldMetaData(worldMetaData);

        Continent continent1 = new Continent(world, SurfaceType.PLAIN);
        continent1.setId(1L);
        continent1.setDirection(new Direction(1, 0));
        continent1.getDirection().setId(1L);
        continent1.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(1, 1, world, continent1),
                CoordinateFactory.createCoordinate(1, 2, world, continent1),
                CoordinateFactory.createCoordinate(2, 1, world, continent1)
        ));

        Continent continent2 = new Continent(world, SurfaceType.COASTAL);
        continent2.setId(2L);
        continent2.setDirection(new Direction(0, -1));
        continent2.getDirection().setId(2L);
        continent2.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(2, 2, world, continent2),
                CoordinateFactory.createCoordinate(3, 2, world, continent2),
                CoordinateFactory.createCoordinate(1, 3, world, continent2)
        ));

        Continent continent3 = new Continent(world, SurfaceType.OCEAN);
        continent3.setId(3L);
        continent3.setDirection(new Direction(0, 0));
        continent3.getDirection().setId(3L);
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

        long idNumber = 1L;
        for (Coordinate coordinate: world.getCoordinates()) {
            coordinate.setWorld(coordinate.getWorld());
            coordinate.getTile().setId(idNumber);
            coordinate.getTile().getBiome().setId(idNumber);
            coordinate.getClimate().setId(idNumber);
            coordinate.getClimate().getSkyTile().setId(idNumber);
            coordinate.setId(idNumber++);
            if (coordinate.getTile().getSurfaceType() == SurfaceType.PLAIN) {
                coordinate.getTile().setHeight(40);
            } else {
                coordinate.getTile().setHeight(20);
            }
        }

        return world;
    }

    public static World createMediumWorld(){
        return createMediumWorld(WorldTypeEnum.NON_EUCLIDEAN);
    }

    public static World createMediumWorld(WorldTypeEnum worldTypeEnum){
        World world = new World(3, 6);
        world.setId(1L);
        world.setLastContinentInFlux(0L);

        WorldSettings worldSettings = TestableWorld.createWorldSettings();
        worldSettings.setId(1L);
        worldSettings.setHeigtDivider(2);
        worldSettings.setMinimumContinents(5);
        world.setWorldSettings(worldSettings);

        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setId(1L);
        worldMetaData.setWorldTypeEnum(worldTypeEnum);
        worldMetaData.setWorld(world);
        worldMetaData.setXSize(world.getXSize());
        worldMetaData.setYSize(world.getYSize());
        worldMetaData.setId(world.getId());

        world.setWorldMetaData(worldMetaData);

        Continent continent1 = new Continent(world, SurfaceType.PLAIN);
        continent1.setId(1L);
        continent1.setDirection(new Direction(1, 0));
        continent1.getDirection().setId(1L);
        continent1.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(1, 1, world, continent1),
                CoordinateFactory.createCoordinate(1, 2, world, continent1),
                CoordinateFactory.createCoordinate(2, 1, world, continent1)
        ));

        Continent continent2 = new Continent(world, SurfaceType.COASTAL);
        continent2.setId(2L);
        continent2.setDirection(new Direction(0, -1));
        continent2.getDirection().setId(2L);
        continent2.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(2, 2, world, continent2),
                CoordinateFactory.createCoordinate(3, 2, world, continent2),
                CoordinateFactory.createCoordinate(1, 3, world, continent2)
        ));

        Continent continent3 = new Continent(world, SurfaceType.OCEAN);
        continent3.setId(3L);
        continent3.setDirection(new Direction(0, 0));
        continent3.getDirection().setId(3L);
        continent3.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(3, 3, world, continent3),
                CoordinateFactory.createCoordinate(3, 1, world, continent3),
                CoordinateFactory.createCoordinate(2, 3, world, continent3)
        ));

        Continent continent4 = new Continent(world, SurfaceType.PLAIN);
        continent4.setId(4L);
        continent4.setDirection(new Direction(1, 0));
        continent4.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(1, 4, world, continent4),
                CoordinateFactory.createCoordinate(1, 5, world, continent4),
                CoordinateFactory.createCoordinate(2, 4, world, continent4)
        ));

        Continent continent5 = new Continent(world, SurfaceType.COASTAL);
        continent5.setId(5L);
        continent5.setDirection(new Direction(0, -1));
        continent5.getDirection().setId(5L);
        continent5.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(2, 5, world, continent5),
                CoordinateFactory.createCoordinate(3, 5, world, continent5),
                CoordinateFactory.createCoordinate(1, 6, world, continent5)
        ));

        Continent continent6 = new Continent(world, SurfaceType.OCEAN);
        continent6.setId(6L);
        continent6.setDirection(new Direction(0, 0));
        continent6.getDirection().setId(6L);
        continent6.getCoordinates().addAll(Arrays.asList(
                CoordinateFactory.createCoordinate(3, 6, world, continent6),
                CoordinateFactory.createCoordinate(3, 4, world, continent6),
                CoordinateFactory.createCoordinate(2, 6, world, continent6)
        ));

        world.setContinents(new HashSet<>(Arrays.asList(continent1, continent2, continent3, continent4, continent5,
                continent6)));
        world.setCoordinates(new HashSet<>());
        world.getCoordinates().addAll(continent1.getCoordinates());
        world.getCoordinates().addAll(continent2.getCoordinates());
        world.getCoordinates().addAll(continent3.getCoordinates());
        world.getCoordinates().addAll(continent4.getCoordinates());
        world.getCoordinates().addAll(continent5.getCoordinates());
        world.getCoordinates().addAll(continent6.getCoordinates());

        long idNumber = 1L;
        for (Coordinate coordinate: world.getCoordinates()) {
            coordinate.setWorld(coordinate.getWorld());
            coordinate.getTile().setId(idNumber);
            coordinate.getTile().getBiome().setId(idNumber);
            coordinate.getClimate().setId(idNumber);
            coordinate.getClimate().getSkyTile().setId(idNumber);
            coordinate.setId(idNumber++);
            if (coordinate.getTile().getSurfaceType() == SurfaceType.PLAIN) {
                coordinate.getTile().setHeight(40);
            } else {
                coordinate.getTile().setHeight(20);
            }
        }

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
                7,
                1);
    }
}
