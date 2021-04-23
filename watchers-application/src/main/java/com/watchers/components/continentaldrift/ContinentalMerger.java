package com.watchers.components.continentaldrift;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalMerger {

    private final WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in time."));
        List<Continent> continents = new ArrayList<>(world.getContinents());
        while(continents.size()>world.getWorldSettings().getMaximumContinents()){
            Continent smallestContinent = continents.stream().min(Comparator.comparingInt((Continent continent) -> continent.getCoordinates().size())).get();
            mergeWithNeighbour(world, smallestContinent);
            taskDto.getGetRemovedContinents().add(smallestContinent.getId());
            continents.remove(smallestContinent);
            world.getContinents().removeIf(continent -> smallestContinent.getId().equals(continent.getId()));
        }
        worldRepository.save(world);
    }

    private void mergeWithNeighbour(World world, Continent continent) {
        Long targetContinentId = continent.calculateMostConnectedNeighbouringContinent();
        Continent targetContinent = world.getContinents().stream().filter(target -> target.getId().equals(targetContinentId)).findFirst().get();
        mergeContinents(continent, targetContinent);
    }

    private void mergeContinents(Continent continent, Continent targetContinent) {
        Set<Coordinate> coordinates = new HashSet<>(continent.getCoordinates());
        coordinates.forEach(coordinate -> coordinate.changeContinent(targetContinent));
        log.debug("Continent " + continent.getId() + " is merged into continent " + targetContinent.getId());
    }
}
