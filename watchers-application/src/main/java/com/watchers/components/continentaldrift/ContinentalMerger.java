package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.ContinentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalMerger {

    private final ContinentRepository continentRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        int maxContinents = world.getWorldSettings().getMaximumContinents();
        Set<Continent> continents = world.getContinents();
        world.getContinents().removeIf(continent -> continent.getCoordinates().isEmpty());
        continents.stream()
                .filter(continent -> continent.getId() == null)
                .forEach(continentRepository::save);

        int mergeCounter = 0;
        while (continents.size() > maxContinents) {
            mergeCounter++;
            Continent smallestContinent = continents.stream()
                    .min(Comparator.comparingInt((Continent continent) -> continent.getCoordinates().size())).orElseThrow();
            mergeWithNeighbour(world, smallestContinent);
            taskDto.getGetRemovedContinents().add(smallestContinent.getId());
            continents.remove(smallestContinent);
            world.getContinents().removeIf(continent -> smallestContinent.getId().equals(continent.getId()));
        }

        if (mergeCounter > 0) {
            log.info("Merged " + mergeCounter + " continents together.");
        }

    }

    @Transactional
    private void mergeWithNeighbour(World world, Continent continent) {
        Long targetContinentId = continent.calculateMostConnectedNeighbouringContinent();
        Continent targetContinent = world.getContinents().stream()
                .filter(target -> target.getId() != null &&
                        target.getId().equals(targetContinentId))
                .findFirst()
                .orElseThrow();
        mergeContinents(continent, targetContinent);
    }

    @Transactional
    private void mergeContinents(Continent continent, Continent targetContinent) {
        Set<Coordinate> coordinates = new HashSet<>(continent.getCoordinates());
        coordinates.forEach(coordinate -> coordinate.changeContinent(targetContinent));
        log.debug("Continent " + continent.getId() + " is merged into continent " + targetContinent.getId());
    }
}
