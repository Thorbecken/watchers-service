package com.watchers.helper;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.Climate;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class SkyHelper {

    public static void calculateAirflows(World world) {
        Set<Coordinate> coordinateList = world.getCoordinates();
        Set<List<Climate>> airCurrents = calculateAircurrents(coordinateList);
        setUpwardSkyTiles(airCurrents);
    }

    private static Set<List<Climate>> calculateAircurrents(Set<Coordinate> coordinates) {
        Map<Long, List<Coordinate>> airCurrentsMap = coordinates.stream()
                .collect(Collectors.groupingBy(Coordinate::getYCoord));

        Map<Long, List<Climate>> airCurrents = airCurrentsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            List<Climate> climatesList = new ArrayList<>();
                            entry.getValue().forEach(coordinate -> {
                                climatesList.add(coordinate.getClimate());
                            });
                            climatesList.sort(Comparator.comparing(Climate::getLongitude));
                            return climatesList;
                        }));

        return airCurrents.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }


    private static void setUpwardSkyTiles(Set<List<Climate>> airCurrents) {
        airCurrents.forEach(airCurrent -> {
            int airCurrentSize = airCurrent.size();

            // sets the first SkyTile as the upstream SkyTile for the last in the array.
            Aircurrent firstAircurrent = new Aircurrent(airCurrent.get(airCurrentSize - 1).getSkyTile(), airCurrent.get(0).getSkyTile());
            firstAircurrent.getStartingSky().getOutgoingAircurrents().add(firstAircurrent);
            firstAircurrent.getEndingSky().getIncommingAircurrents().add(firstAircurrent);

            // transfers the rest of the SkyTiles.
            for (int currentX = 0; currentX < airCurrentSize - 1; currentX++) {
                int nextX = currentX + 1;
                Aircurrent aircurrent = new Aircurrent(airCurrent.get(currentX).getSkyTile(), airCurrent.get(nextX).getSkyTile());
                aircurrent.getStartingSky().getOutgoingAircurrents().add(aircurrent);
                aircurrent.getEndingSky().getIncommingAircurrents().add(aircurrent);
            }
        });
    }

}
