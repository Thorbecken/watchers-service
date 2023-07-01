package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@AllArgsConstructor
public class ContinentalSplitter {

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Set<Continent> continents = new HashSet<>(world.getContinents());

        continents.stream()
                .filter(continent -> !continent.getCoordinates().isEmpty())
                .forEach(this::checkWidthLenght);
    }

    private void checkWidthLenght(Continent continent) {
        long maxWidth = continent.getWorld().getXSize() / 2;
        long width = continent.getCoordinates().stream().map(Coordinate::getXCoord).distinct().count();
        long maxlength = continent.getWorld().getYSize() / 2;
        long length = continent.getCoordinates().stream().map(Coordinate::getYCoord).distinct().count();

        if (maxWidth < width || maxlength < length) {
            doSomething(continent, maxWidth < width);
        }
    }

    public void doSomething(Continent continent, boolean tooMuchWidth) {
        List<Set<Coordinate>> landMasses = CoordinateHelper.getListsOfAdjacentLandCoordinatesFromContinent(continent);
        if (landMasses.size() > 1) {
            Set<Coordinate> largestLandmass = landMasses.stream()
                    .max(Comparator.comparing(Set::size))
                    .get();
            Set<Coordinate> secondLargestLandmass = landMasses.stream()
                    .filter(set -> set != largestLandmass)
                    .max(Comparator.comparing(Set::size))
                    .orElseThrow();
            divideContinentInTwo(continent, largestLandmass, secondLargestLandmass);
        } else if (landMasses.size() == 1) {
            Set<Coordinate> landmass = landMasses.get(0);
            Coordinate meanCoordinate = CoordinateHelper.getMeanCoordinate(new ArrayList<>(landmass), continent.getWorld());
            Coordinate furthestCoordinateFromLandmass = continent.getCoordinates().stream()
                    .filter(coordinate -> !landmass.contains(coordinate))
                    .max(Comparator.comparing(
                            coordinate -> Math.abs(coordinate.getXCoord() - meanCoordinate.getXCoord())
                                    + Math.abs(coordinate.getYCoord() - meanCoordinate.getYCoord())))
                    .orElseThrow();
            Set<Coordinate> newWatermass = new HashSet<>();
            newWatermass.add(furthestCoordinateFromLandmass);
            divideContinentInTwo(continent, landmass, newWatermass);
        } else {
            if (tooMuchWidth) {
                Optional<Coordinate> leftBound = continent.getCoordinates().stream().min(Comparator.comparing(Coordinate::getXCoord));
                Optional<Coordinate> rightBound = continent.getCoordinates().stream().max(Comparator.comparing(Coordinate::getXCoord));
                leftBound.ifPresent(coordinate -> divideContinentInTwo(continent, coordinate, rightBound.get()));
            } else {
                Optional<Coordinate> upBound = continent.getCoordinates().stream().min(Comparator.comparing(Coordinate::getYCoord));
                Optional<Coordinate> downBound = continent.getCoordinates().stream().max(Comparator.comparing(Coordinate::getYCoord));
                upBound.ifPresent(coordinate -> divideContinentInTwo(continent, coordinate, downBound.get()));
            }
        }
    }

    private void divideContinentInTwo(Continent continent, Set<Coordinate> firstSetOfCoordinates, Set<Coordinate> secondSetOfCoordinates) {
        World world = continent.getWorld();
        Continent newContinent = new Continent(world, continent.getType());

        Set<Coordinate> coordinates = new HashSet<>(continent.getCoordinates());

        int currentCoordinates = coordinates.size();
        while (!coordinates.isEmpty()) {
            addAllNeighbouringCoordinatesToSet(coordinates, firstSetOfCoordinates);
            coordinates.removeIf(firstSetOfCoordinates::contains);

            addAllNeighbouringCoordinatesToSet(coordinates, secondSetOfCoordinates);
            coordinates.removeIf(secondSetOfCoordinates::contains);
            if (currentCoordinates == coordinates.size()) {
                firstSetOfCoordinates.addAll(coordinates);
            } else {
                currentCoordinates = coordinates.size();
            }
        }


        secondSetOfCoordinates.stream()
                .filter(secondSetOfCoordinates::contains)
                .forEach(coordinate -> coordinate.changeContinent(newContinent));

        log.warn("Continent " + continent.getId() + " is split in two");

        partContinentsInDifferentDirections(continent, newContinent);
    }

    private void divideContinentInTwo(Continent continent, Coordinate parentCoordinate, Coordinate childCoordinate) {
        World world = continent.getWorld();
        Continent newContinent = new Continent(world, continent.getType());

        Set<Coordinate> coordinates = new HashSet<>(continent.getCoordinates());
        Set<Coordinate> parentCoordinates = new HashSet<>();
        Set<Coordinate> childCoordinates = new HashSet<>();

        parentCoordinates.add(parentCoordinate);
        childCoordinates.add(childCoordinate);

        int currentCoordinates = coordinates.size();
        while (!coordinates.isEmpty()) {
            addAllNeighbouringCoordinatesToSet(coordinates, parentCoordinates);
            coordinates.removeIf(parentCoordinates::contains);

            addAllNeighbouringCoordinatesToSet(coordinates, childCoordinates);
            coordinates.removeIf(childCoordinates::contains);
            if (currentCoordinates == coordinates.size()) {
                parentCoordinates.addAll(coordinates);
            } else {
                currentCoordinates = coordinates.size();
            }
        }

        childCoordinates.stream()
                .filter(childCoordinates::contains)
                .forEach(coordinate -> coordinate.changeContinent(newContinent));

        log.warn("Continent " + continent.getId() + " is split in two");

        partContinentsInDifferentDirections(continent, newContinent);
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

    private void partContinentsInDifferentDirections(Continent continent, Continent newContinent) {
        OptionalDouble averageContinentY = continent.getCoordinates().stream()
                .mapToLong(Coordinate::getYCoord)
                .average();
        OptionalDouble averageContinentX = continent.getCoordinates().stream()
                .mapToLong(Coordinate::getXCoord)
                .average();
        OptionalDouble averageNewContinentY = newContinent.getCoordinates().stream()
                .mapToLong(Coordinate::getYCoord)
                .average();
        OptionalDouble averageNewContinentX = newContinent.getCoordinates().stream()
                .mapToLong(Coordinate::getXCoord)
                .average();

        if (averageContinentY.isPresent() && averageNewContinentY.isPresent()) {
            double meanYDifference = averageContinentY.getAsDouble() - averageNewContinentY.getAsDouble();
            double halfYSize = continent.getWorld().getYSize() / 2d;

            boolean newContinentAbsolutePositionLeftOfContinent = meanYDifference > 0;
            boolean inverseBecauseOfLooping = Math.abs(meanYDifference) > (halfYSize);
            boolean newContinentLeftOfContinent = (newContinentAbsolutePositionLeftOfContinent && !inverseBecauseOfLooping)
                    || (!newContinentAbsolutePositionLeftOfContinent && inverseBecauseOfLooping);

            if (newContinentLeftOfContinent) {
                continent.getDirection().setYVelocity(continent.getDirection().getYVelocity() + 1);
                newContinent.getDirection().setYVelocity(continent.getDirection().getYVelocity() - 1);
            } else {
                continent.getDirection().setYVelocity(continent.getDirection().getYVelocity() - 1);
                newContinent.getDirection().setYVelocity(continent.getDirection().getYVelocity() + 1);
            }
        }

        if (averageContinentX.isPresent() && averageNewContinentX.isPresent()) {
            double meanXDifference = averageContinentX.getAsDouble() - averageNewContinentX.getAsDouble();
            double halfXSize = continent.getWorld().getXSize() / 2d;

            boolean newContinentAbsolutePositionBelowContinent = meanXDifference > 0;
            boolean inverseBecauseOfLooping = Math.abs(meanXDifference) > (halfXSize);
            boolean newContinentBelowContinent = (newContinentAbsolutePositionBelowContinent && !inverseBecauseOfLooping)
                    || (!newContinentAbsolutePositionBelowContinent && inverseBecauseOfLooping);

            if (newContinentBelowContinent) {
                continent.getDirection().setXVelocity(continent.getDirection().getXVelocity() + 1);
                newContinent.getDirection().setXVelocity(continent.getDirection().getXVelocity() - 1);
            } else {
                continent.getDirection().setXVelocity(continent.getDirection().getXVelocity() - 1);
                newContinent.getDirection().setXVelocity(continent.getDirection().getXVelocity() + 1);
            }
        }
    }
}