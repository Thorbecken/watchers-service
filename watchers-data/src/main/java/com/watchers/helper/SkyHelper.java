package com.watchers.helper;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.AircurrentType;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class SkyHelper {

    public static void calculateAirflows(World world) {
        Set<Coordinate> coordinateList = world.getCoordinates();
        Map<Double, List<Climate>> airCurrents = calculateAircurrents(coordinateList);
        AirFlows airFlows = generateAirflows(world);
        setUpwardSkyTiles(airCurrents, airFlows, world);
    }

    static private AirFlows generateAirflows(World world) {
        Long ySize = world.getYSize();
        Double doubleYSize = (double)ySize;
        Map<Double, Boolean> rightwardCurrent = new HashMap<>();
        Map<Double, Boolean> upwardcurrents = new HashMap<>();

        if ((doubleYSize / 6d) < 1d) {
            if ((doubleYSize / 2d) < 1d) {
                rightwardCurrent.put(1d, false);
                upwardcurrents.put(1d, null);
            } else {
                for (long i = 0; i < ySize; i++) {
                    double longitude = transformToLongitude(i, ySize);
                    if (longitude <= 90) {
                        rightwardCurrent.put(1d, false);
                        upwardcurrents.put(1d, false);
                    } else {
                        rightwardCurrent.put(1d, false);
                        upwardcurrents.put(1d, true);
                    }
                }
            }
        } else {
            for (double i = 1; i <= ySize; i++) {
                double longitude = transformToLongitude(i, ySize);
                if (longitude >= 150) {
                    rightwardCurrent.put(longitude, false);
                    upwardcurrents.put(longitude, false);
                } else if (longitude >= 120) {
                    rightwardCurrent.put(longitude, true);
                    upwardcurrents.put(longitude, true);
                } else if (longitude >= 90) {
                    rightwardCurrent.put(longitude, false);
                    upwardcurrents.put(longitude, false);
                } else if (longitude >= 60) {
                    rightwardCurrent.put(longitude, false);
                    upwardcurrents.put(longitude, true);
                } else if (longitude >= 30) {
                    rightwardCurrent.put(longitude, true);
                    upwardcurrents.put(longitude, false);
                } else if (longitude >= 0) {
                    rightwardCurrent.put(longitude, false);
                    upwardcurrents.put(longitude, true);
                } else {
                    throw new RuntimeException(longitude + " is larger than 180!");
                }
            }
        }

        return new AirFlows(rightwardCurrent, upwardcurrents);
    }

    private static Map<Double, List<Climate>> calculateAircurrents(Set<Coordinate> coordinates) {
        Map<Double, List<Coordinate>> airCurrentsMap = coordinates.stream()
                .collect(Collectors.groupingBy(SkyHelper::getLongitude));

        return airCurrentsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            List<Climate> climatesList = new ArrayList<>();
                            entry.getValue().forEach(coordinate -> climatesList.add(coordinate.getClimate()));
                            climatesList.sort(Comparator.comparing(Climate::getLongitude));
                            return climatesList;
                        }));
    }

    private static double getLongitude(Coordinate coordinate) {
        double ySize = coordinate.getWorld().getYSize();
        double invertedY = ySize-coordinate.getYCoord();
        double value = invertedY/ySize*180d;
        return  value;

    }

    static private double transformToLongitude(double y, double ySize) {
        double invertedY = ySize-y;
        double value = invertedY / ySize * 180d;
        return value;
    }


    private static void setUpwardSkyTiles(Map<Double, List<Climate>> airCurrents, AirFlows airFlows, World world) {
        airCurrents.forEach((Double longitude, List<Climate> airCurrent) -> {
            boolean rightward = airFlows.rightwardCurrent.get(longitude);
            Boolean upward = airFlows.upwardcurrents.get(longitude);
            int airCurrentSize = airCurrent.size();
            SkyTile startingSky;
            SkyTile endingSky;

            if (rightward) {
                startingSky = airCurrent.get(airCurrentSize - 1).getSkyTile();
                endingSky = airCurrent.get(0).getSkyTile();
            } else {
                startingSky = airCurrent.get(0).getSkyTile();
                endingSky = airCurrent.get(airCurrentSize - 1).getSkyTile();
            }

            // sets the first SkyTile as the upstream SkyTile for the last in the array.
            Aircurrent firstAircurrent = new Aircurrent(startingSky, endingSky, AircurrentType.LATITUDAL);
            firstAircurrent.getStartingSky().getOutgoingAircurrents().add(firstAircurrent);
            firstAircurrent.getEndingSky().getIncommingAircurrents().add(firstAircurrent);
            //addLatidualFlow(firstAircurrent, upward, world, startingSky);

            // transfers the rest of the SkyTiles.
            for (int currentX = 0; currentX < airCurrentSize - 1; currentX++) {
                int nextX = currentX + 1;
                if (rightward) {
                    startingSky = airCurrent.get(currentX).getSkyTile();
                    endingSky = airCurrent.get(nextX).getSkyTile();
                } else {
                    startingSky = airCurrent.get(nextX).getSkyTile();
                    endingSky = airCurrent.get(currentX).getSkyTile();
                }

                Aircurrent aircurrent = new Aircurrent(startingSky, endingSky, AircurrentType.LATITUDAL);

                aircurrent.getStartingSky().getOutgoingAircurrents().add(aircurrent);
                aircurrent.getEndingSky().getIncommingAircurrents().add(aircurrent);

                //addLatidualFlow(aircurrent, upward, world, startingSky);
            }
        });
    }

    private static void addLatidualFlow(Aircurrent aircurrent, Boolean upward, World world, SkyTile startingSky) {
        Coordinate upcurrentCoordinate = aircurrent.getEndingSky().getClimate().getCoordinate();
        if (upward.equals(Boolean.TRUE)) {
            long upY = upcurrentCoordinate.getUpCoordinate();
            long upX = upcurrentCoordinate.getXCoord();
            Coordinate above = world.getCoordinate(upX, upY);
            Aircurrent upstreamCurrent = new Aircurrent(startingSky, above.getClimate().getSkyTile(), AircurrentType.LONGITUDAL);
            upstreamCurrent.getStartingSky().getOutgoingAircurrents().add(upstreamCurrent);
            upstreamCurrent.getEndingSky().getIncommingAircurrents().add(upstreamCurrent);
        } else if (upward.equals(Boolean.FALSE)) {
            long downY = upcurrentCoordinate.getDownCoordinate();
            long downX = upcurrentCoordinate.getXCoord();
            Coordinate downCoordinate = world.getCoordinate(downX, downY);
            Aircurrent upstreamCurrent = new Aircurrent(startingSky, downCoordinate.getClimate().getSkyTile(), AircurrentType.LONGITUDAL);
            upstreamCurrent.getStartingSky().getOutgoingAircurrents().add(upstreamCurrent);
            upstreamCurrent.getEndingSky().getIncommingAircurrents().add(upstreamCurrent);
        }
    }

    static private class AirFlows {
        private Map<Double, Boolean> rightwardCurrent;
        private Map<Double, Boolean> upwardcurrents;

        private AirFlows(Map<Double, Boolean> rightwardCurrent, Map<Double, Boolean> upwardcurrents) {
            this.rightwardCurrent = rightwardCurrent;
            this.upwardcurrents = upwardcurrents;
        }
    }

}
