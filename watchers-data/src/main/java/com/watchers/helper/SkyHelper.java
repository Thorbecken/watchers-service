package com.watchers.helper;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.AircurrentType;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SkyHelper {

    public static void calculateAndWeaveAirflows(World world) {
        List<AirflowTranslation> airflowTranslations = calculateAirflowTranslations(world);
        weaveAircurrents(airflowTranslations, world);
    }

    private static List<AirflowTranslation> calculateAirflowTranslations(World world) {
        List<AirflowTranslation> airflowTranslations = new ArrayList<>();

        Long ySize = world.getYSize();
        double doubleYSize = (double) ySize;

        if ((doubleYSize / 6d) < 1d) {
            if ((doubleYSize / 2d) < 1d) {
                airflowTranslations.add(new AirflowTranslation(1d, false, false));
            } else {
                for (double yCoordinate = 1; yCoordinate <= ySize; yCoordinate++) {
                    double latitude = ClimateHelper.transformToLatitude(yCoordinate, ySize);
                    if (latitude >= 0) {
                        airflowTranslations.add(new AirflowTranslation(yCoordinate, false, false));
                    } else {
                        airflowTranslations.add(new AirflowTranslation(yCoordinate, true, false));
                    }
                }
            }
        } else {
            for (double yCoordinate = 1; yCoordinate <= ySize; yCoordinate++) {
                double latitude = ClimateHelper.transformToLatitude(yCoordinate, ySize);
                if (latitude > 60) {
                    airflowTranslations.add(new AirflowTranslation(yCoordinate, false, false));
                } else if (latitude > 30) {
                    airflowTranslations.add(new AirflowTranslation(yCoordinate, true, true));
                } else if (latitude > 0) {
                    airflowTranslations.add(new AirflowTranslation(yCoordinate, false, false));
                } else if (latitude > -30) {
                    airflowTranslations.add(new AirflowTranslation(yCoordinate, true, false));
                } else if (latitude > -60) {
                    airflowTranslations.add(new AirflowTranslation(yCoordinate, false, true));
                } else if (latitude > -90) {
                    airflowTranslations.add(new AirflowTranslation(yCoordinate, true, false));
                } else {
                    throw new RuntimeException(latitude + " is larger than 180!");
                }
            }

        }

        return airflowTranslations;
    }

    private static void weaveAircurrents(List<AirflowTranslation> airflowTranslations, World world) {
        final int latitudalStrength = world.getWorldSettings().getLatitudinalStrength();
        final int longitudalStrength = world.getWorldSettings().getLongitudinalStrength();

        airflowTranslations.forEach(airflowTranslation -> {
                    List<Coordinate> coordinates = world.getCoordinates().stream()
                            .filter(coordinate -> coordinate.getYCoord() == airflowTranslation.YCoordinate)
                            .collect(Collectors.toList());

                    coordinates.forEach(coordinate -> {
                        addLatitudalAircurrent(coordinate, airflowTranslation.rightward, latitudalStrength);
                        addLongitudalAircurrent(coordinate, airflowTranslation.upward, longitudalStrength);
                    });
                }
        );
    }

    protected static void addLatitudalAircurrent(Coordinate startingCoordinate, boolean rightward, int latitudalStrength) {
        SkyTile startingSky = startingCoordinate.getClimate().getSkyTile();
        SkyTile endingSky;
        if (rightward) {
            endingSky = startingCoordinate.getRightNeighbour().getClimate().getSkyTile();
        } else {
            endingSky = startingCoordinate.getLeftNeighbour().getClimate().getSkyTile();
        }

        new Aircurrent(startingSky, endingSky, AircurrentType.LATITUDAL, latitudalStrength);
    }

    protected static void addLongitudalAircurrent(Coordinate startingCoordinate, boolean upward, int longitudalStrength) {
        SkyTile startingSky = startingCoordinate.getClimate().getSkyTile();
        SkyTile endingSky;
        if (upward) {
            endingSky = startingCoordinate.getUpNeighbour().getClimate().getSkyTile();
        } else {
            endingSky = startingCoordinate.getDownNeighbour().getClimate().getSkyTile();
        }

        new Aircurrent(startingSky, endingSky, AircurrentType.LONGITUDAL, longitudalStrength);
    }

    @AllArgsConstructor
    protected static class AirflowTranslation {
        private final double YCoordinate;
        private final boolean upward;
        private final boolean rightward;
    }

}
