package com.watchers.helper;

import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.World;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CoordinateHelper {

    public static int UP = 1;
    public static int DOWN = -1;
    public static int LEFT = -1;
    public static int RIGHT = 1;

    public List<Coordinate> getAllPossibleCoordinates(World world) {
        List<Coordinate> coordinates = new ArrayList<>();
        for (long x = 1; x <= world.getXSize(); x++) {
            for (long y = 1; y <= world.getYSize(); y++) {
                Coordinate coordinate =new Coordinate(x,y, world);
                coordinate.setWorld(world);
                coordinates.add(coordinate);
            }
        }
        return coordinates;
    }

    public static List<Coordinate> getAllOutersideCoordinates(List<Coordinate> coordinates){
        List<Coordinate> possibleCoordinates = coordinates.stream().map(Coordinate::getNeighbours)
                .reduce((List<Coordinate> x, List<Coordinate> y) ->
                {
                    List<Coordinate> list = new ArrayList();
                    list.addAll(x);
                    list.addAll(y);
                    return list;
                })
                .get().stream()
                .distinct()
                .collect(Collectors.toList());
        possibleCoordinates.removeAll(coordinates);
        return possibleCoordinates;
    }
}
