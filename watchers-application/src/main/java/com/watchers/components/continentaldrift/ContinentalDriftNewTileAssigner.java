package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.MockContinent;
import com.watchers.model.environment.SurfaceType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContinentalDriftNewTileAssigner {

    public void process(ContinentalDriftTaskDto taskDto){
        int currentNumberOfContinents = taskDto.getWorld().getContinents().size();
        Long newestContinent = taskDto.getWorld().getContinents().stream()
                .max(Comparator.comparing(Continent::getId))
                .map(Continent::getId)
                .orElse(null);
        Assert.notNull(newestContinent, "There was no continent found on the world with an id number!");
        long nextContinentalId = newestContinent+1;

        int minimumContinents = taskDto.getMinContinents();

        List<List<Coordinate>> listOfConnectedCoordinates = generateEmptyTileClusters(taskDto);

        int newContinentsCreated = createNewContinents(taskDto, currentNumberOfContinents, nextContinentalId, minimumContinents, listOfConnectedCoordinates);
        addEmptytilesToExistingContinents(newContinentsCreated, listOfConnectedCoordinates, taskDto);

    }

    private void addEmptytilesToExistingContinents(int listOfCoordinatesProcessed, List<List<Coordinate>> listOfConnectedCoordinates, ContinentalDriftTaskDto taskDto) {
        if(listOfCoordinatesProcessed < listOfConnectedCoordinates.size()){
            for (int i = listOfCoordinatesProcessed; i < listOfConnectedCoordinates.size(); i++) {
                List<Coordinate> connectedCoordinates = listOfConnectedCoordinates.get(i);
                List<Coordinate> adjecantCoordinates = CoordinateHelper.getAllOutersideCoordinates(connectedCoordinates);

                List<Coordinate> existingCoordinates = taskDto.getChanges().values().stream()
                        .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                        .map(ContinentalChangesDto::getKey)
                        .filter(adjecantCoordinates::contains)
                        .collect(Collectors.toList());

                Continent chosenContinent = taskDto.getChanges().values().stream()
                        .filter(continentalChangesDto -> existingCoordinates.contains(continentalChangesDto.getKey()))
                        .map(ContinentalChangesDto::getMockTile)
                        .filter(Objects::nonNull)
                        .map(MockTile::getContinent)
                        .filter(Objects::nonNull)
                        .filter(continent -> continent.getId()!=null)
                        .max(Comparator.comparing(Continent::getId))
                        .orElse(null);

                Assert.notNull(chosenContinent, "no adjacent continent was found!");
                MockContinent mockContinent = new MockContinent(connectedCoordinates,taskDto.getWorld());
                mockContinent.setContinent(chosenContinent);
                connectedCoordinates.forEach(coordinate ->
                        taskDto.getChanges().get(coordinate).setNewMockContinent(mockContinent)
                );

                listOfCoordinatesProcessed++;
            }
        }
    }

    private int createNewContinents(ContinentalDriftTaskDto taskDto, int currentContinents, long nextContinentalId, int minimumContinents, List<List<Coordinate>> listOfConnectedCoordinates) {
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
        return listOfCoordinatesProcessed;
    }

    private List<List<Coordinate>> generateEmptyTileClusters(ContinentalDriftTaskDto taskDto) {
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
        return listOfConnectedCoordinates;
    }

    private boolean checkForList(int i, List<List<Coordinate>> listOfConnectedCoordinates) {
        return i < listOfConnectedCoordinates.size();
    }

    private boolean newContinentLoop(int i, int currentContinents, int minimumContinents) {
        return i+currentContinents < minimumContinents;
    }
}
