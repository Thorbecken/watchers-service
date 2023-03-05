package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
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

    private WorldRepository worldRepository;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto){
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in time."));
        Set<Continent> continents = new HashSet<>(world.getContinents());
        continents.forEach(this::checkIntegrity);
        worldRepository.saveAndFlush(world);
    }

    private void checkIntegrity(Continent continent){
        List<List<Coordinate>> listOfConnectedCoordinates = generateEmptyTileClusters(continent);
        if(listOfConnectedCoordinates.size()>1){
            log.debug("Continent " + continent.getId() + " was split into " + listOfConnectedCoordinates.size() + " parts");
            for (int i = 1; i < listOfConnectedCoordinates.size(); i++) {
                World world = continent.getWorld();
                createNewContinent(listOfConnectedCoordinates.get(i), world, continent.getType());
            }
        }
    }

    private void createNewContinent(List<Coordinate> coordinates, World world, SurfaceType type) {
        Continent newContinent = new Continent(world, type);
        coordinates.forEach(coordinate -> coordinate.changeContinent(newContinent));
    }

    private List<List<Coordinate>> generateEmptyTileClusters(Continent continent) {
        List<List<Coordinate>> listOfConnectedCoordinates = new ArrayList<>();

        List<Coordinate> emptyCoordinates = new ArrayList<>(continent.getCoordinates());

        while(emptyCoordinates.size() != 0){
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(emptyCoordinates.get(0));
            emptyCoordinates.remove(emptyCoordinates.get(0));
            boolean stillProcessing = true;
            while(stillProcessing) {
                List<Coordinate> neighbouringCoordinates = CoordinateHelper.getAllOutersideCoordinates(coordinates);
                List<Coordinate> newCoordinates = neighbouringCoordinates.stream().filter(emptyCoordinates::contains).collect(Collectors.toList());

                coordinates.addAll(newCoordinates);
                emptyCoordinates.removeAll(newCoordinates);
                stillProcessing = newCoordinates.size() != 0;
            }
            listOfConnectedCoordinates.add(coordinates);
        }

        listOfConnectedCoordinates.sort(Comparator.comparing(List::size));
        return listOfConnectedCoordinates;
    }

}
