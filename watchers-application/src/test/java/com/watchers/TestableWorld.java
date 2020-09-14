package com.watchers;

import com.watchers.model.common.Coordinate;
import com.watchers.model.common.Direction;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.World;

import java.util.*;

public class TestableWorld {

    public static World createWorld() {
        World world = new World(3, 3);
        world.setLastContinentInFlux(0L);

        Continent continent1 = new Continent(world, SurfaceType.PLAIN);
        continent1.setId(0L);
        continent1.setDirection(new Direction(1, 0));
        continent1.getCoordinates().addAll(Arrays.asList(
                new Coordinate(1, 1, world, continent1),
                new Coordinate(1, 2, world, continent1),
                new Coordinate(2, 1, world, continent1)
        ));

        Continent continent2 = new Continent(world, SurfaceType.COASTAL);
        continent2.setId(1L);
        continent2.setDirection(new Direction(0, -1));
        continent2.getCoordinates().addAll(Arrays.asList(
                new Coordinate(2, 2, world, continent2),
                new Coordinate(3, 2, world, continent2),
                new Coordinate(1, 3, world, continent2)
        ));

        Continent continent3 = new Continent(world, SurfaceType.OCEANIC);
        continent3.setId(2L);
        continent3.setDirection(new Direction(0, 0));
        continent3.getCoordinates().addAll(Arrays.asList(
                new Coordinate(3, 3, world, continent3),
                new Coordinate(3, 1, world, continent3),
                new Coordinate(2, 3, world, continent3)
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


}