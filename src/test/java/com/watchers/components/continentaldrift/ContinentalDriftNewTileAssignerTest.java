package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ContinentalDriftNewTileAssignerTest {

    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private ContinentalDriftTaskDto taskDto;
    private CoordinateHelper coordinateHelper;


    @BeforeEach
    void setUp() {
        World world = TestableWorld.createWorld();
        this.coordinateHelper = new CoordinateHelper();
        this.continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner();
        ContinentalDriftAdjuster continentalDriftAdjuster = new ContinentalDriftAdjuster(coordinateHelper);
        ContinentalDriftTileAdjuster continentalDriftTileAdjuster = new ContinentalDriftTileAdjuster(coordinateHelper);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        continentalDriftAdjuster.process(taskDto);
        continentalDriftTileAdjuster.process(taskDto);
    }

    @Test
    void processTest() {
        assertTrue(taskDto.getChanges().values().stream().allMatch(dto -> dto.getNewMockContinent() == null));
        assertTrue(taskDto.getChanges().values().stream().anyMatch(ContinentalChangesDto::isEmpty));

        long oldestContinentId = taskDto.getWorld().getContinents().stream().max(Comparator.comparing(Continent::getId)).map(Continent::getId).get();

        continentalDriftNewTileAssigner.process(taskDto);

        assertTrue(taskDto.getChanges().values().stream().filter(ContinentalChangesDto::isEmpty).noneMatch(continentalChangesDto -> continentalChangesDto.getNewMockContinent() == null));
    }

    @Test
    void processTestLargeNewMockContinent(){
        World world = taskDto.getWorld();

        Continent continentX = new Continent(world, SurfaceType.PLAIN);
        Continent continentY = new Continent(world, SurfaceType.PLAIN);

        continentX.setId(1L);
        continentY.setId(2L);

        coordinateHelper.getAllPossibleCoordinates(world).forEach(
                coordinate -> {
                    ContinentalChangesDto dto = taskDto.getChanges().get(coordinate);
                    long sum = coordinate.getYCoord()+coordinate.getXCoord();
                    if(sum == 4 || sum == 3){
                        dto.setEmpty(true);
                        dto.setNewTile(null);
                        dto.setOldCoordinate(null);
                        dto.setNewMockContinent(null);
                        return;
                    }
                    Continent continent = sum<4?continentX:continentY;
                    Tile tile = new Tile(coordinate, world,continent);
                    dto.setNewTile(tile);
                    dto.setEmpty(false);
                    dto.setOldCoordinate(coordinate);
                }
        );

        //Checks that the test is beginning with only five open tiles
        assertEquals(5, taskDto.getChanges().values().stream().filter(ContinentalChangesDto::isEmpty).count());
        // Checks there are nog mockcontinents assigned
        assertEquals(0, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());

        continentalDriftNewTileAssigner.process(taskDto);

        assertEquals(5, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());
        assertEquals(3, taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .map(MockContinent::getContinent)
                .max(Comparator.comparing(Continent::getId))
                .map(Continent::getId)
                .get());

        // asserts that there is only created one continent
        assertEquals(1,taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .map(MockContinent::getContinent)
                .filter(distinctByKey(Continent::getId))
                .count());

        //assert that all the new tiles have the same mock continent.
        assertEquals(5, taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .findFirst()
                .get()
                .getCoordinates()
                .size());

        // Assert that only four tiles that are adjecent to the mockcontinent
        // checks the possible continent to be merged with
        assertEquals(4, taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .findFirst()
                .get()
                .getPossibleCoordinates()
                .size());
    }

    @ParameterizedTest
    @CsvSource({"2", "3", "4", "5", "6"})
    void processTestNumberOfMinimumContinents(int minimumContinents){
        World world = taskDto.getWorld();
        taskDto.setMinContinents(minimumContinents);

        Continent continentX = new Continent(world, SurfaceType.PLAIN);
        Continent continentY = new Continent(world, SurfaceType.PLAIN);

        continentX.setId(1L);
        continentY.setId(2L);
        world.setContinents(new HashSet<>(Arrays.asList(continentX, continentY)));


        coordinateHelper.getAllPossibleCoordinates(world).forEach(
                coordinate -> {
                    ContinentalChangesDto dto = taskDto.getChanges().get(coordinate);
                    long sum = coordinate.getYCoord()+coordinate.getXCoord();
                    if(sum == 4){
                        dto.setEmpty(true);
                        dto.setNewTile(null);
                        return;
                    }
                    Continent continent = sum<4?continentX:continentY;
                    Tile tile = new Tile(coordinate, world,continent);
                    dto.setNewTile(tile);
                    dto.setEmpty(false);
                }
        );

        assertEquals(3, taskDto.getChanges().values().stream().filter(ContinentalChangesDto::isEmpty).count());
        assertEquals(0, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());

        continentalDriftNewTileAssigner.process(taskDto);

        assertEquals(3, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());

        List<MockContinent> mockContinents = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .collect(Collectors.toList());

        long newContinents = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .map(MockContinent::getContinent)
                .filter(continent -> taskDto.getWorld().getContinents().stream().map(Continent::getId).noneMatch(aLong -> aLong.longValue() == continent.getId().longValue()))
                .filter(distinctByKey(Continent::getId))
                .count();

        int expectedContinents = minimumContinents<6?minimumContinents:5;
        assertEquals(expectedContinents, world.getContinents().size()+newContinents);
    }

    private static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}