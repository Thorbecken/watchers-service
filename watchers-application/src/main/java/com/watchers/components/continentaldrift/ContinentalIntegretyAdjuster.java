package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
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
public class ContinentalIntegretyAdjuster {

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Set<Continent> continents = new HashSet<>(world.getContinents());
        continents.forEach(this::checkContinent);
    }

    private void checkContinent(Continent continent) {
        Map<Coordinate, Set<Coordinate>> checkedCoordinates = new HashMap<>();
        List<Coordinate> continentCoordinates = new ArrayList<>(continent.getCoordinates());
        while (!continentCoordinates.isEmpty()) {
            Coordinate currentRandomCoordinate = continentCoordinates.get(0);
            Set<Coordinate> adjacendCoordinates = new HashSet<>();
            adjacendCoordinates.add(currentRandomCoordinate);

            boolean noMoreExtraAdjacentCoordinatesFound = false;
            while (!noMoreExtraAdjacentCoordinatesFound) {
                Set<Coordinate> newAdjacentContinentCoordinates = adjacendCoordinates.stream()
                        .flatMap(coordinate -> coordinate.getNeighbours().stream())
                        .filter(coordinate -> continent.getId().equals(coordinate.getContinent().getId()))
                        .filter(coordinate -> !adjacendCoordinates.contains(coordinate))
                        .collect(Collectors.toSet());
                adjacendCoordinates.addAll(newAdjacentContinentCoordinates);
                adjacendCoordinates.forEach(continentCoordinates::remove);
                if (newAdjacentContinentCoordinates.isEmpty()) {
                    noMoreExtraAdjacentCoordinatesFound = true;
                }
            }

            checkedCoordinates.put(currentRandomCoordinate, adjacendCoordinates);
        }

        List<Set<Coordinate>> checkedCoordinatesList = new ArrayList<>(checkedCoordinates.values());
        if (checkedCoordinatesList.size() > 1) {
            log.info("Continent " + continent.getId() + " was split into " + checkedCoordinates.size() + " parts");
            World world = continent.getWorld();
            for (int i = 0; i < checkedCoordinatesList.size(); i++) {
                if (i != 0) {
                    createNewContinent(checkedCoordinatesList.get(i), world, continent.getType());
                }
            }
        }

    }

    private void createNewContinent(Collection<Coordinate> coordinates, World world, SurfaceType type) {
        Continent newContinent = new Continent(world, type);
        coordinates.forEach(coordinate -> coordinate.changeContinent(newContinent));
    }
}
