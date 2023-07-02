package com.watchers.helper;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.AircurrentType;
import com.watchers.model.climate.Climate;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClimateHelper {
    static public double roundToTwoDigits(double number) {
        int temp = (int) (number * 100);
        return temp / 100d;
    }

    static public double transformToLatitude(double y, double ySize) {
        double rawLatitude = ((y / ySize) * 180d) - 90d;
        return roundToTwoDigits(rawLatitude);
    }

    public static double transformToLongitude(double x, double wx) {
        double rawLongitude = x / wx * 360d;
        return roundToTwoDigits(rawLongitude);
    }

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
        Climate startingSky = startingCoordinate.getClimate();
        Climate endingSky;
        if (rightward) {
            endingSky = startingCoordinate.getRightNeighbour().getClimate();
        } else {
            endingSky = startingCoordinate.getLeftNeighbour().getClimate();
        }

        new Aircurrent(startingSky, endingSky, AircurrentType.LATITUDAL, latitudalStrength);
    }

    protected static void addLongitudalAircurrent(Coordinate startingCoordinate, boolean upward, int longitudalStrength) {
        Climate startingSky = startingCoordinate.getClimate();
        Climate endingSky;
        if (upward) {
            endingSky = startingCoordinate.getUpNeighbour().getClimate();
        } else {
            endingSky = startingCoordinate.getDownNeighbour().getClimate();
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
