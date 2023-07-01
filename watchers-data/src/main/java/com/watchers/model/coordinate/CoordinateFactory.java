package com.watchers.model.coordinate;


import com.watchers.model.world.Continent;
import com.watchers.model.world.World;

public class CoordinateFactory {

    public static Coordinate createCoordinate(long xCoord, long yCoord, World world, Continent continent) {
        Coordinate coordinate = new Coordinate(xCoord, yCoord, world, continent);
        world.getCoordinates().add(coordinate);
        return coordinate;
    }

}
