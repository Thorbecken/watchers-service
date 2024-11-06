package com.watchers.helper;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CoordinateHelper {

    public static int UP = 1;
    public static int DOWN = -1;
    public static int LEFT = -1;
    public static int RIGHT = 1;

    public static List<Coordinate> getAllPossibleCoordinates(World world) {
        return new ArrayList<>(world.getCoordinates());
    }

    public static List<Coordinate> getAllOutersideCoordinates(List<Coordinate> coordinates) {
        List<Coordinate> possibleCoordinates = coordinates.stream().map(Coordinate::getNeighbours)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        possibleCoordinates.removeAll(coordinates);
        return possibleCoordinates;
    }

    public static List<Set<Coordinate>> getListsOfAdjacentCoordinatesFromContinent(Continent continent) {
        return getListsOfAdjacentCoordinates(new ArrayList<>(continent.getCoordinates())
                , coordinate -> coordinate.getContinent().getId().equals(continent.getId()));
    }

    public static List<Set<Coordinate>> getListsOfAdjacentLandCoordinatesFromContinent(Continent continent) {
        return getListsOfAdjacentCoordinates(new ArrayList<>(continent.getCoordinates())
                , coordinate -> coordinate.getContinent().getId().equals(continent.getId())
                        && coordinate.getTile().getHeight() >= continent.getWorld().getSeaLevel());
    }

    public static List<Set<Coordinate>> getListsOfAdjacentCoordinates(List<Coordinate> coordinates, Predicate<Coordinate> predicate) {
        Map<Coordinate, Set<Coordinate>> checkedCoordinates = new HashMap<>();
        List<Coordinate> continentCoordinates = new ArrayList<>(coordinates);
        while (!continentCoordinates.isEmpty()) {
            Coordinate currentRandomCoordinate = continentCoordinates.get(0);
            Set<Coordinate> adjacendCoordinates = new HashSet<>();
            adjacendCoordinates.add(currentRandomCoordinate);

            boolean noMoreExtraAdjacentCoordinatesFound = false;
            while (!noMoreExtraAdjacentCoordinatesFound) {
                Set<Coordinate> newAdjacentContinentCoordinates = adjacendCoordinates.stream()
                        .flatMap(coordinate -> coordinate.getNeighbours().stream())
                        .filter(predicate)
                        .filter(coordinate -> !adjacendCoordinates.contains(coordinate))
                        .collect(Collectors.toSet());
                adjacendCoordinates.addAll(newAdjacentContinentCoordinates);
                adjacendCoordinates.forEach(continentCoordinates::remove);
                if (newAdjacentContinentCoordinates.isEmpty()) {
                    noMoreExtraAdjacentCoordinatesFound = true;
                }
            }

            checkedCoordinates.put(currentRandomCoordinate, adjacendCoordinates);
        }

        return new ArrayList<>(checkedCoordinates.values());
    }

    public static Coordinate getMeanCoordinate(Continent continent) {
        return getMeanCoordinate(new ArrayList<>(continent.getCoordinates()), continent.getWorld());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Coordinate getMeanCoordinate(List<Coordinate> coordinates, World world) {
        long meanX = (long) coordinates.stream()
                .mapToLong(Coordinate::getXCoord)
                .average()
                .getAsDouble();
        long meanY = (long) coordinates.stream()
                .mapToLong(Coordinate::getYCoord)
                .average()
                .getAsDouble();

        boolean abnormalX = coordinates.stream().anyMatch(coordinate -> coordinate.getXCoord() == meanX);
        boolean abnormalY = coordinates.stream().anyMatch(coordinate -> coordinate.getYCoord() == meanY);


        long usableX = meanX;
        long usableY = meanY;
        if (abnormalX) {
            long halfXSize = world.getXSize() / 2;
            if (meanX > halfXSize) {
                usableX = meanX - halfXSize;
            } else {
                usableX = meanX + halfXSize;
            }
        }

        if (abnormalY) {
            long halfYSize = world.getYSize() / 2;
            if (meanX > halfYSize) {
                usableY = meanY - halfYSize;
            } else {
                usableY = meanY + halfYSize;
            }
        }

        Coordinate coordinate = world.getCoordinate(usableX, usableY);
        if (coordinate == null) {
            coordinate = coordinates.get(0);
        }

        return coordinate;
    }
}
