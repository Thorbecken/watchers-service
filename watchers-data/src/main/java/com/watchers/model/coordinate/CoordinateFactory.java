package com.watchers.model.coordinate;

import com.watchers.model.environment.Continent;
import com.watchers.model.world.World;

import java.util.List;

public class CoordinateFactory {

    public static void fillListWithCoordinates(List<Coordinate> openCoordinates, World world, Continent fillerContinent) {
        if (WorldTypeEnum.GLOBE.equals(world.getWorldSetting().getWorldTypeEnum())) {
            for (long xCoord = 1L; xCoord <= world.getXSize()*2; xCoord++) {
                for (long yCoord = 1L; yCoord <= world.getYSize(); yCoord++) {
                    openCoordinates.add(new GlobeCoordinate(xCoord, yCoord, world, fillerContinent));
                }
            }
        } else {
            for (long xCoord = 1L; xCoord <= world.getXSize(); xCoord++) {
                for (long yCoord = 1L; yCoord <= world.getYSize(); yCoord++) {
                    Coordinate coordinate;
                    switch (world.getWorldSetting().getWorldTypeEnum()) {
                        case WRAP_AROUND:
                            coordinate = new WrapAroundCoordinate(xCoord, yCoord, world, fillerContinent);
                            break;
                        case NON_EUCLIDEAN:
                            coordinate = new NonEuclideanCoordinate(xCoord, yCoord, world, fillerContinent);
                            break;
                        default:
                            coordinate = new NonEuclideanCoordinate(xCoord, yCoord, world, fillerContinent);
                    }
                    openCoordinates.add(coordinate);
                }
            }
        }
    }

    public static Coordinate createCoordinate(long xCoord, long yCoord, World world, Continent continent) {
        return new NonEuclideanCoordinate(xCoord, yCoord, world, continent);
    }

}
