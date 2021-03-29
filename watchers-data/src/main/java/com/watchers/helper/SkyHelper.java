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

    // left <-> right
    static int LATITUDAL_STRENGTH = 1;
    // up <-> down
    static int LONGITUDAL_STRENGTH = 0;

    public static void calculateAirflows(World world) {
        Set<Coordinate> coordinateList = world.getCoordinates();
        Map<Double, List<Climate>> airCurrents = calculateAircurrents(coordinateList);
        AirFlows airFlows = generateAirflows(world);
        setUpwardSkyTiles(airCurrents, airFlows, world);
    }

    static private AirFlows generateAirflows(World world) {
        Long ySize = world.getYSize();
        Double doubleYSize = (double) ySize;
        Map<Double, Boolean> rightwardCurrent = new HashMap<>();
        Map<Double, Boolean> upwardcurrents = new HashMap<>();

        if ((doubleYSize / 6d) < 1d) {
            if ((doubleYSize / 2d) < 1d) {
                rightwardCurrent.put(1d, false);
                upwardcurrents.put(1d, null);
            } else {
                for (long i = 1; i <= ySize; i++) {
                    double latitude = ClimateHelper.transformToLatitude(i, ySize);
                    if (latitude >= 0) {
                        rightwardCurrent.put(latitude, false);
                        upwardcurrents.put(latitude, false);
                    } else {
                        rightwardCurrent.put(latitude, false);
                        upwardcurrents.put(latitude, true);
                    }
                }
            }
        } else {
            for (double i = 1; i <= ySize; i++) {
                double latitude = ClimateHelper.transformToLatitude(i, ySize);
                if (latitude > 60) {
                    rightwardCurrent.put(latitude, false);
                    upwardcurrents.put(latitude, false);
                } else if (latitude > 30) {
                    rightwardCurrent.put(latitude, true);
                    upwardcurrents.put(latitude, true);
                } else if (latitude > 0) {
                    rightwardCurrent.put(latitude, false);
                    upwardcurrents.put(latitude, false);
                } else if (latitude > -30) {
                    rightwardCurrent.put(latitude, false);
                    upwardcurrents.put(latitude, true);
                } else if (latitude > -60) {
                    rightwardCurrent.put(latitude, true);
                    upwardcurrents.put(latitude, false);
                } else if (latitude > -90) {
                    rightwardCurrent.put(latitude, false);
                    upwardcurrents.put(latitude, true);
                } else {
                    throw new RuntimeException(latitude + " is larger than 180!");
                }
            }
        }

        return new AirFlows(rightwardCurrent, upwardcurrents);
    }

    private static Map<Double, List<Climate>> calculateAircurrents(Set<Coordinate> coordinates) {
        Map<Double, List<Coordinate>> airCurrentsMap = coordinates.stream()
                .collect(Collectors.groupingBy(coordinate -> coordinate.getClimate().getLatitude()));

        return airCurrentsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            List<Climate> climatesList = new ArrayList<>();
                            entry.getValue().forEach(coordinate -> climatesList.add(coordinate.getClimate()));
                            climatesList.sort(Comparator.comparing(Climate::getLongitude));
                            return climatesList;
                        }));
    }

    private static void setUpwardSkyTiles(Map<Double, List<Climate>> airCurrents, AirFlows airFlows, World world) {
        airCurrents.forEach((Double latitude, List<Climate> airCurrent) -> {
            boolean rightward = airFlows.rightwardCurrent.get(latitude);
            Boolean upward = airFlows.upwardcurrents.get(latitude);
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
            Aircurrent firstAircurrent = addLatitudalAircurrent(startingSky, endingSky);
            addLongitudalAircurrent(firstAircurrent, upward, world, startingSky);

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

                Aircurrent aircurrent = addLatitudalAircurrent(startingSky, endingSky);
                addLongitudalAircurrent(aircurrent, upward, world, startingSky);
            }
        });
    }

    private static Aircurrent addLatitudalAircurrent(SkyTile startingSky, SkyTile endingSky) {
        Aircurrent latitudalAircurrent = new Aircurrent(startingSky, endingSky, AircurrentType.LATITUDAL, LATITUDAL_STRENGTH);
        latitudalAircurrent.getStartingSky().getOutgoingAircurrents().add(latitudalAircurrent);
        latitudalAircurrent.getEndingSky().getIncommingAircurrents().add(latitudalAircurrent);
        return latitudalAircurrent;
    }

    private static void addLongitudalAircurrent(Aircurrent aircurrent, Boolean upward, World world, SkyTile startingSky) {
        Coordinate upcurrentCoordinate = aircurrent.getEndingSky().getClimate().getCoordinate();
        SkyTile skyTile;
        if (upward.equals(Boolean.TRUE)) {
            long upY = upcurrentCoordinate.getUpCoordinate();
            long upX = upcurrentCoordinate.getXCoord();
            Coordinate above = world.getCoordinate(upX, upY);
            skyTile = above.getClimate().getSkyTile();
        } else if (upward.equals(Boolean.FALSE)) {
            long downY = upcurrentCoordinate.getDownCoordinate();
            long downX = upcurrentCoordinate.getXCoord();
            Coordinate downCoordinate = world.getCoordinate(downX, downY);
            skyTile = downCoordinate.getClimate().getSkyTile();
        } else {
            throw new RuntimeException();
        }
        Aircurrent upstreamCurrent = new Aircurrent(startingSky, skyTile, AircurrentType.LONGITUDAL, LONGITUDAL_STRENGTH);
        upstreamCurrent.getStartingSky().getOutgoingAircurrents().add(upstreamCurrent);
        upstreamCurrent.getEndingSky().getIncommingAircurrents().add(upstreamCurrent);
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
