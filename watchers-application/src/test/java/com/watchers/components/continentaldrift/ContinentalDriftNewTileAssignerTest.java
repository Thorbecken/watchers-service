package com.watchers.components.continentaldrift;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.config.SettingConfiguration;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.world.Continent;
import com.watchers.model.dto.MockContinent;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

class ContinentalDriftNewTileAssignerTest {

    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private ContinentalDriftTaskDto taskDto;
    private WorldRepository worldRepository = Mockito.mock(WorldRepository.class);


    @BeforeEach
    void setUp() {
        SettingConfiguration settingConfiguration = TestableWorld.createConfiguration();
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepository, settingConfiguration);
        this.continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(worldRepository, continentalDriftDirectionChanger, settingConfiguration);
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(worldRepository);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(worldRepository);

        World world = TestableWorld.createWorld();

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        taskDto.setMinContinents(1);
        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftPredicter.process(taskDto);
        continentalDriftTileChangeComputer.process(taskDto);
    }

    @Test
    void processTest() {
        assertTrue(taskDto.getChanges().values().stream().allMatch(dto -> dto.getNewMockContinent() == null));
        assertTrue(taskDto.getChanges().values().stream().anyMatch(ContinentalChangesDto::isEmpty));

        continentalDriftNewTileAssigner.process(taskDto);

        assertTrue(taskDto.getChanges().values().stream().filter(ContinentalChangesDto::isEmpty).noneMatch(continentalChangesDto -> continentalChangesDto.getNewMockContinent() == null));
    }

    @Test
    void processTestLargeNewMockContinent(){
        World world = new World();

        Continent continentX = new Continent(world, SurfaceType.PLAIN);
        Continent continentY = new Continent(world, SurfaceType.PLAIN);

        continentX.setId(1L);
        continentY.setId(2L);

        CoordinateHelper.getAllPossibleCoordinates(world).forEach(
                coordinate -> {
                    ContinentalChangesDto dto = taskDto.getChanges().get(coordinate);
                    long sum = coordinate.getYCoord()+coordinate.getXCoord();
                    if(sum == 4 || sum == 3){
                        dto.setEmpty(true);
                        dto.setMockTile(null);
                        dto.setNewMockContinent(null);
                        return;
                    }
                    Continent continent = sum<4?continentX:continentY;
                    coordinate.changeContinent(continent);
                    continent.getCoordinates().add(coordinate);
                    dto.setMockTile(new MockTile(coordinate.getTile()));
                    dto.setEmpty(false);
                }
        );

        //Checks that the test is beginning with only five open coordinates
        assertEquals(5, taskDto.getChanges().values().stream().filter(ContinentalChangesDto::isEmpty).count());
        // Checks there are nog mockcontinents assigned
        assertEquals(0, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());

        continentalDriftNewTileAssigner.process(taskDto);

        assertEquals(5, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());
        assertEquals(2, taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .map(MockContinent::getContinent)
                .max(Comparator.comparing(Continent::getId))
                .map(Continent::getId)
                .get()
                .longValue());

        // asserts that only one continent is created
        assertEquals(1,taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .map(MockContinent::getContinent)
                .filter(distinctByKey(Continent::getId))
                .count());

        //assert that all the new coordinates have the same mock continent.
        assertEquals(5, taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .findFirst()
                .get()
                .getCoordinates()
                .size());

        // Assert that only four coordinates that are adjecent to the mockcontinent
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
        World world = createTestWorld();

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        taskDto.setWorldId(1L);
        taskDto.setMinContinents(minimumContinents);


        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(worldRepository);
        continentalDriftPredicter.process(taskDto);

        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(worldRepository);
        continentalDriftTileChangeComputer.process(taskDto);

        assertEquals(3, taskDto.getChanges().values().stream().filter(ContinentalChangesDto::isEmpty).count());
        assertEquals(0, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());

        continentalDriftNewTileAssigner.process(taskDto);

        assertEquals(3, taskDto.getChanges().values().stream().filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null).count());

        long newContinents = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> continentalChangesDto.getNewMockContinent() != null)
                .map(ContinentalChangesDto::getNewMockContinent)
                .map(MockContinent::getContinent)
                .filter(continent -> world.getContinents().stream()
                        .map(Continent::getId)
                        .noneMatch(aLong -> aLong.longValue() == continent.getId().longValue()))
                .filter(distinctByKey(Continent::getId))
                .count();

        int expectedContinents = minimumContinents<6?minimumContinents:5;
        assertEquals(expectedContinents, world.getContinents().size() + newContinents);
    }

    private World createTestWorld() {
        World world = new World(3,3);
        world.setId(1L);

        Continent continentX = new Continent(world, SurfaceType.PLAIN);
        continentX.setDirection(new Direction(0,0));
        Continent continentY = new Continent(world, SurfaceType.OCEAN);
        continentY.setDirection(new Direction(1,0));
        Continent continentZ = new Continent(world, SurfaceType.OCEAN);
        continentZ.setDirection(new Direction(0, 1));
        continentX.setId(3L);
        continentY.setId(2L);
        continentZ.setId(1L);

        world.setContinents(new HashSet<>(Arrays.asList(continentX, continentY)));

        generateCoordinates(world);
        assignTiles(world, continentX, continentY, continentZ);
        world.getContinents().removeIf(continent -> continent.getType() == null);

        return world;
    }

    private void generateCoordinates(World world) {
        for (int xCoordinate = 1; xCoordinate <= world.getXSize(); xCoordinate++) {
            for (int yCoordinate = 1; yCoordinate <= world.getYSize(); yCoordinate++) {
                world.getCoordinates().add(CoordinateFactory.createCoordinate(xCoordinate, yCoordinate, world, new Continent(world, null)));
            }
        }
    }

    private void assignTiles(World world, Continent x, Continent y, Continent z) {
        CoordinateHelper.getAllPossibleCoordinates(world).forEach(
                coordinate -> {
                    if(coordinate.getXCoord()+coordinate.getYCoord() <4){
                        coordinate.getTile().setHeight(8);
                        coordinate.changeContinent(x);
                        x.getCoordinates().add(coordinate);
                    } else if (coordinate.getYCoord()==3 && coordinate.getXCoord() == 3){
                        coordinate.getTile().setHeight(2);
                        coordinate.changeContinent(z);
                        z.getCoordinates().add(coordinate);
                    } else {
                        coordinate.getTile().setHeight(4);
                        coordinate.changeContinent(y);
                        y.getCoordinates().add(coordinate);
                    }
                }
        );
    }

    private static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}