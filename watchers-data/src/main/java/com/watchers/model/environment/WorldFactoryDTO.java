package com.watchers.model.environment;

import com.watchers.model.common.Coordinate;
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

        for (long xCoord = 1L; xCoord <= world.getXSize(); xCoord++){
            for (long yCoord = 1L; yCoord <= world.getYSize(); yCoord++){
                Coordinate coordinate = new Coordinate(xCoord, yCoord, world, new Continent(world, null));
                openCoordinates.add(coordinate);
            }
        }

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


        return openCoordinates.stream().filter(
                coordinate -> world.getContinents().stream().anyMatch(
                        continent -> continent.getCoordinates().stream().anyMatch(
                                continentCoordinate -> !continentCoordinate.equals(coordinate)
                        )
                )
        ).collect(Collectors.toList());
    }

}
