package com.watchers.model.dto;

import com.watchers.model.common.Coordinate;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
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

        for (long xCoord = 1L; xCoord <= world.getXSize(); xCoord++){
            for (long yCoord = 1L; yCoord <= world.getYSize(); yCoord++){
                Coordinate coordinate = new Coordinate(xCoord, yCoord, world, fillerContinent);
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
