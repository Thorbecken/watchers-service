package com.watchers.helper;

import com.watchers.model.common.Coordinate;
import com.watchers.model.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoordinateHelper {

    public static int UP = 1;
    public static int DOWN = -1;
    public static int LEFT = -1;
    public static int RIGHT = 1;

    public static List<Coordinate> getAllPossibleCoordinates(World world) {
        return new ArrayList<>(world.getCoordinates());
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
