package com.watchers.model.world;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.coordinate.NonEuclideanCoordinate;
import com.watchers.model.environment.Continent;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class WorldFactoryDTO {

    private World world;
    private List<Coordinate> openCoordinates;
    private List<Coordinate> takenCoordinates;

    public WorldFactoryDTO(World world){
        this.world = world;
        this.takenCoordinates = new ArrayList<>();
        this.openCoordinates = generateOpenAndTakenCoordinates(world);
    }

    private List<Coordinate> generateOpenAndTakenCoordinates(World world) {
        List<Coordinate> openCoordinates = new ArrayList<>();
        Continent fillerContinent = new Continent(world, null);

        CoordinateFactory.fillListWithCoordinates(openCoordinates, world, fillerContinent);

        world.getCoordinates().addAll(openCoordinates);

        List<Coordinate> startingCoordinates = openCoordinates.stream().filter(
                openCoordinate -> world.getContinents().stream().anyMatch(
                        continent -> continent.getCoordinates().stream().anyMatch(
                                continentCoordinate -> continentCoordinate.equals(openCoordinate)
                        )
                )
        ).collect(Collectors.toList());

        takenCoordinates.addAll(startingCoordinates);
        openCoordinates.removeAll(startingCoordinates);

        world.getContinents().remove(fillerContinent);

        return openCoordinates.stream().filter(
                coordinate -> world.getContinents().stream().anyMatch(
                        continent -> continent.getCoordinates().stream().anyMatch(
                                continentCoordinate -> !continentCoordinate.equals(coordinate)
                        )
                )
        ).collect(Collectors.toList());
    }

}
