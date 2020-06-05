package com.watchers;

import com.watchers.model.common.Direction;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;

import java.util.*;

public class TestableWorld {

    public static World createWorld() {
        World world = new World(3, 3);
        world.setLastContinentInFlux(0L);

        Continent continent1 = new Continent(world, SurfaceType.PLAIN);
        continent1.setId(0L);
        continent1.setDirection(new Direction(1, 0));
        continent1.getTiles().addAll(Arrays.asList(
                new Tile(1, 1, world, continent1),
                new Tile(1, 2, world, continent1),
                new Tile(2, 1, world, continent1)
        ));

        Continent continent2 = new Continent(world, SurfaceType.COASTAL);
        continent2.setId(1L);
        continent2.setDirection(new Direction(0, -1));
        continent2.getTiles().addAll(Arrays.asList(
                new Tile(2, 2, world, continent2),
                new Tile(3, 2, world, continent2),
                new Tile(1, 3, world, continent2)
        ));

        Continent continent3 = new Continent(world, SurfaceType.OCEANIC);
        continent3.setId(2L);
        continent3.setDirection(new Direction(0, 0));
        continent3.getTiles().addAll(Arrays.asList(
                new Tile(3, 3, world, continent3),
                new Tile(3, 1, world, continent3),
                new Tile(2, 3, world, continent3)
        ));

        world.setContinents(new HashSet<>(Arrays.asList(continent1, continent2, continent3)));
        world.setTiles(new HashSet<>());
        world.getTiles().addAll(continent1.getTiles());
        world.getTiles().addAll(continent2.getTiles());
        world.getTiles().addAll(continent3.getTiles());

        world.getTiles().forEach(tile -> {
                    tile.getCoordinate().setWorld(tile.getWorld());
                    if (tile.getSurfaceType() == SurfaceType.PLAIN) {
                        tile.setHeight(40);
                    } else {
                        tile.setHeight(20);
                    }
                }
        );


        return world;
    }


}
