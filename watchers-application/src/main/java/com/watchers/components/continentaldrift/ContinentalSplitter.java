package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
@AllArgsConstructor
public class ContinentalSplitter {

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto){
        World world = taskDto.getWorld();
        Set<Continent> continents =  new HashSet<>(world.getContinents());

        continents.stream()
                .filter(continent -> !continent.getCoordinates().isEmpty())
                .forEach(this::checkWidthLenght);
    }

    private void checkWidthLenght(Continent continent) {
        Long width = continent.getCoordinates().stream().map(Coordinate::getXCoord).distinct().count();
        Long lenght = continent.getCoordinates().stream().map(Coordinate::getYCoord).distinct().count();

        if(width/lenght>continent.getWorld().getWorldSettings().getMaxWidthLenghtBalance()){
            Coordinate leftBound = continent.getCoordinates().stream().min(Comparator.comparing(Coordinate::getXCoord)).get();
            Coordinate rightBound = continent.getCoordinates().stream().max(Comparator.comparing(Coordinate::getXCoord)).get();
            divideContinentInTwo(continent, leftBound, rightBound);
        } else if(lenght/width>continent.getWorld().getWorldSettings().getMaxWidthLenghtBalance()) {
            Coordinate upBound = continent.getCoordinates().stream().min(Comparator.comparing(Coordinate::getYCoord)).get();
            Coordinate downBound = continent.getCoordinates().stream().max(Comparator.comparing(Coordinate::getYCoord)).get();
            divideContinentInTwo(continent, upBound, downBound);
        }
    }

    private void divideContinentInTwo(Continent continent, Coordinate parentCoordinate, Coordinate childCoordinate) {
        World world = continent.getWorld();
        Continent newContinent = new Continent(world, continent.getType());

        //TODO: make the two continent part direction by changing their directions.

        Set<Coordinate> coordinates = new HashSet<>(continent.getCoordinates());
        Set<Coordinate> parentCoordinates =  new HashSet<>();
        Set<Coordinate> childCoordinates = new HashSet<>();

        parentCoordinates.add(parentCoordinate);
        childCoordinates.add(childCoordinate);

        while (!coordinates.isEmpty()){
            addAllNeighbouringCoordinatesToSet(coordinates, parentCoordinates);
            coordinates.removeIf(parentCoordinates::contains);

            addAllNeighbouringCoordinatesToSet(coordinates, childCoordinates);
            coordinates.removeIf(childCoordinates::contains);
        }


        childCoordinates.stream()
                .filter(childCoordinates::contains)
                .forEach(coordinate -> coordinate.changeContinent(newContinent));

        log.warn("Continent " +  continent.getId() + " is split in two");
    }

    private void addAllNeighbouringCoordinatesToSet(Set<Coordinate> coordinates, Set<Coordinate> parentCoordinates) {
        Set<Coordinate> parentNeightbours = parentCoordinates.stream()
                .map(Coordinate::getNeighbours)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        coordinates.stream()
                .filter(parentNeightbours::contains)
                .forEach(parentCoordinates::add);
    }
}