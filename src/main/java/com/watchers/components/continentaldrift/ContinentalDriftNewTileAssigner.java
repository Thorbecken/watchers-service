package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContinentalDriftNewTileAssigner {

    public void process(ContinentalDriftTaskDto taskDto){
        int currentContinents = taskDto.getWorld().getContinents().size();
        Long lastContinentalId = taskDto.getWorld().getContinents().stream()
                .max(Comparator.comparing(Continent::getId))
                .map(Continent::getId)
                .orElse(null);
        Assert.notNull(lastContinentalId, "There was no continent found on the world with an id number!");
        long nextContinentalId = lastContinentalId+1;


        int minimumContinents = taskDto.getMinContinents();

        List<List<Coordinate>> listOfConnectedCoordinates = new ArrayList<>();

        List<Coordinate> emptyCoordinates = taskDto.getChanges().values().stream()
                .filter(ContinentalChangesDto::isEmpty)
                .map(ContinentalChangesDto::getKey)
                .collect(Collectors.toList());

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

        int listOfCoordinatesProcessed = 0;
        if(currentContinents < minimumContinents) {
            for (int i = listOfCoordinatesProcessed; newContinentLoop(i, currentContinents, minimumContinents) && checkForList(i, listOfConnectedCoordinates); i++) {

                List<Coordinate> connectedCoordinates = listOfConnectedCoordinates.get(i);

                Continent continent = new Continent(taskDto.getWorld(), SurfaceType.PLAIN);
                MockContinent mockContinent = new MockContinent(connectedCoordinates, taskDto.getWorld());
                mockContinent.setContinent(continent);
                mockContinent.getContinent().setId(nextContinentalId++);
                connectedCoordinates.forEach(coordinate -> taskDto.getChanges().get(coordinate).setNewMockContinent(mockContinent));

                listOfCoordinatesProcessed++;
            }
        }
        if(listOfCoordinatesProcessed < listOfConnectedCoordinates.size()){
            for (int i = listOfCoordinatesProcessed; i < listOfConnectedCoordinates.size(); i++) {
                List<Coordinate> connectedCoordinates = listOfConnectedCoordinates.get(i);
                List<Coordinate> adjecantCoordinates = CoordinateHelper.getAllOutersideCoordinates(connectedCoordinates);

                List<Tile> existingTiles = taskDto.getChanges().values().stream()
                        .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                        .map(ContinentalChangesDto::getKey)
                        .filter(adjecantCoordinates::contains)
                        .map(coordinate -> taskDto.getWorld().getTile(coordinate))
                        .collect(Collectors.toList());

                Continent assignedContinent = existingTiles.stream()
                        .map(Tile::getContinent)
                        .max(Comparator.comparing(Continent::getId))
                        .orElse(null);

                Assert.notNull(assignedContinent, "no adjacent continent was found!");
                MockContinent mockContinent = new MockContinent(connectedCoordinates,taskDto.getWorld());
                mockContinent.setContinent(assignedContinent);
                connectedCoordinates.forEach(coordinate -> taskDto.getChanges().get(coordinate).setNewMockContinent(mockContinent));

                listOfCoordinatesProcessed++;
            }
        }
    }

    private boolean checkForList(int i, List<List<Coordinate>> listOfConnectedCoordinates) {
        return i < listOfConnectedCoordinates.size();
    }

    private boolean newContinentLoop(int i, int currentContinents, int minimumContinents) {
        return i+currentContinents < minimumContinents;
    }
}
