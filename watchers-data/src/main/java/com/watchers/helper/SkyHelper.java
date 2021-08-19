package com.watchers.helper;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.AircurrentType;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkyHelper {

    public static void calculateAirflows(World world) {
        Set<Coordinate> coordinateList = world.getCoordinates();
        List<LatitudalAirflow> latitudalAirflows = calculateAircurrents(coordinateList);
        AirFlows airFlows = generateAirflows(world);
        setAircurrents(latitudalAirflows, airFlows, world);
    }

    static private AirFlows generateAirflows(World world) {
        Long ySize = world.getYSize();
        Double doubleYSize = (double) ySize;
        Map<Double, Boolean> rightwardCurrent = new HashMap<>();
        Map<Double, Boolean> upwardcurrents = new HashMap<>();

        if ((doubleYSize / 6d) < 1d) {
            if ((doubleYSize / 2d) < 1d) {
                rightwardCurrent.put(1d, false);
                upwardcurrents.put(1d, false);
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

    private static List<LatitudalAirflow> calculateAircurrents(Set<Coordinate> coordinates) {
        Map<Double, List<Coordinate>> airCurrentsMap = coordinates.stream()
                .collect(Collectors.groupingBy(coordinate -> coordinate.getClimate().getLatitude()));
        return airCurrentsMap.entrySet().stream()
                .map(entrySet -> {
                    List<SkyTile> climates = entrySet.getValue().stream()
                            .map(Coordinate::getClimate)
                            .map(Climate::getSkyTile)
                            .collect(Collectors.toList());

                    return new LatitudalAirflow(entrySet.getKey(), climates);
                })
                .collect(Collectors.toList());
    }

    private static void setAircurrents(List<LatitudalAirflow> latitudalAirflows, AirFlows airFlows, World world) {
        List<Aircurrent> aircurrents = latitudalAirflows.stream()
                .flatMap(latitudalAirflow -> streamToAircurrents(latitudalAirflow, airFlows, world))
                .collect(Collectors.toList());
    }

    private static Stream<Aircurrent> streamToAircurrents(LatitudalAirflow latitudalAirflow, AirFlows airFlows, World world){
        return createAircurrents(latitudalAirflow, airFlows, world).stream();
    }

    private static List<Aircurrent> createAircurrents(LatitudalAirflow latitudalAirflow, AirFlows airFlows, World world){
        final int latitudalStrength = world.getWorldSettings().getLatitudinalStrength();
        final int longitudalStrength = world.getWorldSettings().getLongitudinalStrength();

        List<Aircurrent> aircurrentList = new ArrayList<>();
        boolean rightward = airFlows.rightwardCurrent.get(latitudalAirflow.getLatitude());
        Boolean upward = airFlows.upwardcurrents.get(latitudalAirflow.getLatitude());
        List<SkyTile> skyTiles = latitudalAirflow.getSkyTiles();

        int airCurrentSize = skyTiles.size();
        SkyTile startingSky;
        SkyTile endingSky;

        if (rightward) {
            startingSky = skyTiles.get(airCurrentSize - 1);
            endingSky = skyTiles.get(0);
        } else {
            startingSky = skyTiles.get(0);
            endingSky = skyTiles.get(airCurrentSize - 1);
        }

        // sets the first SkyTile as the upstream SkyTile for the last in the array.
        Aircurrent firstLatutudalAircurrent = addLatitudalAircurrent(startingSky, endingSky, latitudalStrength);
        aircurrentList.add(firstLatutudalAircurrent);
        Aircurrent firstLongitudalAircurrent = addLongitudalAircurrent(firstLatutudalAircurrent, upward, world, startingSky, longitudalStrength);
        aircurrentList.add(firstLongitudalAircurrent);

        // transfers the rest of the SkyTiles.
        for (int currentX = 0; currentX < airCurrentSize - 1; currentX++) {
            int nextX = currentX + 1;
            if (rightward) {
                startingSky = skyTiles.get(currentX);
                endingSky = skyTiles.get(nextX);
            } else {
                startingSky = skyTiles.get(nextX);
                endingSky = skyTiles.get(currentX);
            }

            Aircurrent aircurrent = addLatitudalAircurrent(startingSky, endingSky, latitudalStrength);
            aircurrentList.add(aircurrent);
            Aircurrent longitudalAircurrent = addLongitudalAircurrent(aircurrent, upward, world, startingSky, longitudalStrength);
            aircurrentList.add(longitudalAircurrent);
        }

        return aircurrentList;
    }

    private static Aircurrent addLatitudalAircurrent(SkyTile startingSky, SkyTile endingSky, int latitudalStrength) {
        return new Aircurrent(startingSky, endingSky, AircurrentType.LATITUDAL, latitudalStrength);
    }

    private static Aircurrent addLongitudalAircurrent(Aircurrent aircurrent, Boolean upward, World world, SkyTile startingSky, int longitudalStrength) {
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

        return new Aircurrent(startingSky, skyTile, AircurrentType.LONGITUDAL, longitudalStrength);
    }

    static private class AirFlows {
        private final Map<Double, Boolean> rightwardCurrent;
        private final Map<Double, Boolean> upwardcurrents;

        private AirFlows(Map<Double, Boolean> rightwardCurrent, Map<Double, Boolean> upwardcurrents) {
            this.rightwardCurrent = rightwardCurrent;
            this.upwardcurrents = upwardcurrents;
        }
    }

    @Getter
    @AllArgsConstructor
    private static class LatitudalAirflow {
        private final Double latitude;
        private final List<SkyTile> skyTiles;
    }

}
