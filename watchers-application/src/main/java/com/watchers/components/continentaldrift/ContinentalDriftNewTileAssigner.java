package com.watchers.components.continentaldrift;

import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.*;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static com.watchers.model.enums.SurfaceType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContinentalDriftNewTileAssigner {

    private final WorldRepository worldRepository;
    private final ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    long nextContinentalId = 0;


    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = worldRepository.findById(taskDto.getWorldId()).orElseThrow(() -> new RuntimeException("The world was lost in memory."));
        int currentNumberOfContinents = world.getContinents().size();
        Long newestContinent = world.getContinents().stream()
                .max(Comparator.comparing(Continent::getId))
                .map(Continent::getId)
                .orElse(null);
        Assert.notNull(newestContinent, "There was no continent found on the world with an id number!");
        nextContinentalId = newestContinent + 1;

        int minimumContinents = world.getWorldSettings().getMinimumContinents();

        List<List<Coordinate>> listOfConnectedCoordinates = generateEmptyTileClusters(taskDto, world);

        int newContinentsCreated = createNewContinents(taskDto, currentNumberOfContinents, minimumContinents, listOfConnectedCoordinates, world);
        addEmptytilesToExistingContinents(newContinentsCreated, listOfConnectedCoordinates, taskDto, world);

        worldRepository.save(world);

    }

    private void addEmptytilesToExistingContinents(int listOfCoordinatesProcessed, List<List<Coordinate>> listOfConnectedCoordinates, ContinentalDriftTaskDto taskDto, World world) {
        if (listOfCoordinatesProcessed < listOfConnectedCoordinates.size()) {
            for (int i = listOfCoordinatesProcessed; i < listOfConnectedCoordinates.size(); i++) {
                List<Coordinate> connectedCoordinates = listOfConnectedCoordinates.get(i);
                List<Coordinate> adjecantCoordinates = CoordinateHelper.getAllOutersideCoordinates(connectedCoordinates);

                List<Coordinate> existingCoordinates = taskDto.getChanges().values().stream()
                        .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                        .map(ContinentalChangesDto::getKey)
                        .map(world::getCoordinate)
                        .filter(adjecantCoordinates::contains)
                        .collect(Collectors.toList());

                Continent chosenContinent = getChosenContinent(taskDto, existingCoordinates, world);

                MockContinentDto mockContinentDto = new MockContinentDto(chosenContinent.getId(), null, connectedCoordinates);
                connectedCoordinates.forEach(coordinate ->
                        taskDto.getChanges().get(new MockCoordinate(coordinate)).setMockContinentDto(mockContinentDto)
                );

                listOfCoordinatesProcessed++;
            }
        }
    }

    private Continent getChosenContinent(ContinentalDriftTaskDto taskDto, List<Coordinate> existingCoordinates, World world) {
        Continent chosenContinent = getValidAdjacentContinent(taskDto, existingCoordinates, world);

        if (chosenContinent == null) {
            chosenContinent = new Continent(world, getSurfaceType(world, taskDto));
            chosenContinent.setId(nextContinentalId++);
            continentalDriftDirectionChanger.assignFirstDriftDirrecion(chosenContinent, world);
        }

        return chosenContinent;
    }

    private Continent getValidAdjacentContinent(ContinentalDriftTaskDto taskDto, List<Coordinate> existingCoordinates, World world) {
        return taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> existingCoordinates.contains(world.getCoordinate(continentalChangesDto.getKey())))
                .map(ContinentalChangesDto::getMockTile)
                .filter(Objects::nonNull)
                .map(MockTile::getMockContinentObject)
                .filter(Objects::nonNull)
                .filter(continent -> continent.getId() != null)
                .map(mockContinentObject -> world.getContinents().stream().filter(continent -> continent.getId().equals(mockContinentObject.getId())).findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.comparing(continent -> continent.getCoordinates().size()))
                .orElse(null);
    }

    private SurfaceType getSurfaceType(World world, ContinentalDriftTaskDto taskDto) {
        long numberOfOceaanicContinents = world.getContinents().stream()
                .map(Continent::getType)
                .filter(surfaceType -> OCEAN.equals(surfaceType) || SEA.equals(surfaceType) || COASTAL.equals(surfaceType))
                .count();
        long numberOfContinents = world.getContinents().size();

        //adjusting for new continents
        Set<MockContinentDto> changes = taskDto.getChanges().values().stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (MockContinentDto newContinent : changes) {
            numberOfContinents++;
            SurfaceType surfaceType = newContinent.getSurfaceType();
            if (OCEAN.equals(surfaceType) || SEA.equals(surfaceType) || COASTAL.equals(surfaceType)) {
                numberOfOceaanicContinents++;
            }
        }

        int oceanicContinentsPerContinentalContinent = world.getWorldSettings().getContinentalToOcceanicRatio() + 1;
        boolean tooMuchOceanicContinents = numberOfOceaanicContinents * oceanicContinentsPerContinentalContinent > numberOfContinents;
        return tooMuchOceanicContinents ? SurfaceType.PLAIN : OCEAN;
    }

    private int createNewContinents(ContinentalDriftTaskDto taskDto, int currentContinents, int minimumContinents, List<List<Coordinate>> listOfConnectedCoordinates, World world) {
        int listOfCoordinatesProcessed = 0;
        if (currentContinents < minimumContinents) {
            for (int i = listOfCoordinatesProcessed;
                 newContinentLoop(i, currentContinents, minimumContinents)
                         && checkForList(i, listOfConnectedCoordinates)
                    ; i++) {

                List<Coordinate> connectedCoordinates = listOfConnectedCoordinates.get(i);

                SurfaceType surfaceType = getSurfaceType(world, taskDto);
                MockContinentDto mockContinentDto = new MockContinentDto(nextContinentalId++, surfaceType, connectedCoordinates);
                connectedCoordinates.stream()
                        .map(MockCoordinate::new)
                        .map(mockCoordinate -> taskDto.getChanges().get((mockCoordinate)))
                        .forEach(continentalChangesDto -> continentalChangesDto.setMockContinentDto(mockContinentDto));

                listOfCoordinatesProcessed++;
            }
        }
        return listOfCoordinatesProcessed;
    }

    private List<List<Coordinate>> generateEmptyTileClusters(ContinentalDriftTaskDto taskDto, World world) {
        List<List<Coordinate>> listOfConnectedCoordinates = new ArrayList<>();

        List<Coordinate> emptyCoordinates = taskDto.getChanges().values().stream()
                .filter(ContinentalChangesDto::isEmpty)
                .map(ContinentalChangesDto::getKey)
                .map(world::getCoordinate)
                .collect(Collectors.toList());

        while (emptyCoordinates.size() != 0) {
            List<Coordinate> coordinates = new ArrayList<>();
            coordinates.add(emptyCoordinates.get(0));
            emptyCoordinates.remove(emptyCoordinates.get(0));
            boolean stillProcessing = true;
            while (stillProcessing) {
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
        return i + currentContinents < minimumContinents;
    }
}
