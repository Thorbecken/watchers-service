package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Direction;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.dto.*;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.*;
import com.watchers.repository.ContinentRepository;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContinentalDriftNewTileAssignerTest {

    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private ContinentalDriftTaskDto taskDto;
    private final WorldRepository worldRepository = Mockito.mock(WorldRepository.class);
    private final ContinentRepository continentRepository = Mockito.mock(ContinentRepository.class);


    @BeforeEach
    void setUp() {
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepository);
        this.continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(worldRepository, continentalDriftDirectionChanger);
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(continentRepository);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(worldRepository);

        World world = TestableWorld.createWorld();
        world.getWorldSettings().setMinimumContinents(1);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        Mockito.when(continentRepository.findAll()).thenReturn(new ArrayList<>(world.getContinents()));
        continentalDriftPredicter.process(taskDto);
        continentalDriftTileChangeComputer.process(taskDto);
    }

    @Test
    void processTest() {
        List<ContinentalChangesDto> changesDtos = new ArrayList<>(taskDto.getChanges().values());
        assertTrue(changesDtos.stream()
                .allMatch(dto -> dto.getMockContinentDto() == null));
        assertTrue(changesDtos.stream()
                .anyMatch(ContinentalChangesDto::isEmpty));

        continentalDriftNewTileAssigner.process(taskDto);

        assertTrue(changesDtos.stream()
                .filter(ContinentalChangesDto::isEmpty)
                .noneMatch(continentalChangesDto -> continentalChangesDto.getMockContinentDto() == null));
    }

    @Test
    void processTestLargeNewMockContinent() {
        World world = TestableWorld.createWorld(WorldTypeEnum.SQUARE);

        Continent continentX = new Continent(world, SurfaceType.PLAIN);
        Continent continentY = new Continent(world, SurfaceType.PLAIN);

        continentX.setId(1L);
        continentY.setId(2L);

        CoordinateHelper.getAllPossibleCoordinates(world).forEach(
                coordinate -> {
                    ContinentalChangesDto dto = taskDto.getChanges().get(new MockCoordinate(coordinate));
                    long sum = coordinate.getYCoord() + coordinate.getXCoord();
                    if (sum == 4 || sum == 3) {
                        dto.setEmpty(true);
                        dto.setMockTile(null);
                        dto.setMockContinentDto(null);
                        return;
                    }
                    Continent continent = sum < 4 ? continentX : continentY;
                    coordinate.changeContinent(continent);
                    continent.getCoordinates().add(coordinate);
                    dto.setMockTile(new MockTile(coordinate.getTile()));
                    dto.setEmpty(false);
                }
        );

        List<ContinentalChangesDto> changesDtos = new ArrayList<>(taskDto.getChanges().values());
        
        //Checks that the test is beginning with only five open coordinates
        assertEquals(5, changesDtos.stream()
                .filter(ContinentalChangesDto::isEmpty)
                .count());
        // Checks there are no mockcontinents assigned
        assertEquals(0, changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .count());

        continentalDriftNewTileAssigner.process(taskDto);

        // check that all coordinates have been assigned a new continent
        assertEquals(5, changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .count());

        // check that coordinates have been merged to the biggest continent
        assertEquals(2, changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(MockContinentDto::getContinentId))
                .map(MockContinentDto::getContinentId)
                .orElseThrow()
                .longValue());

        // asserts that only one continent is created
        assertEquals(1, changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .filter(distinctByKey(MockContinentDto::getContinentId))
                .count());

        //assert that all the new coordinates have the same mock continent.
        assertEquals(5, changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .filter(mockContinentDto -> mockContinentDto.getContinentId().equals(2L))
                .count());
    }

    @ParameterizedTest
    @CsvSource({"2", "3", "4", "5", "6"})
    void processTestNumberOfMinimumContinents(int minimumContinents) {
        World world = createTestWorld();

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        taskDto.setWorldId(1L);
        world.getWorldSettings().setMinimumContinents(minimumContinents);

        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(continentRepository);
        continentalDriftPredicter.process(taskDto);

        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(worldRepository);
        continentalDriftTileChangeComputer.process(taskDto);

        List<ContinentalChangesDto> changesDtos = new ArrayList<>(taskDto.getChanges().values());

        assertEquals(3, changesDtos.stream()
                .filter(ContinentalChangesDto::isEmpty)
                .count());
        assertEquals(0, changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .count());

        continentalDriftNewTileAssigner.process(taskDto);

        assertEquals(3, changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .count());

        long newContinents = changesDtos.stream()
                .map(ContinentalChangesDto::getMockContinentDto)
                .filter(Objects::nonNull)
                .filter(mockContinentDto -> world.getContinents().stream()
                        .noneMatch(continent -> continent.getId().equals(mockContinentDto.getContinentId())))
                .filter(distinctByKey(MockContinentDto::getContinentId))
                .count();

        int expectedContinents = minimumContinents<6?minimumContinents:5;
        assertEquals(expectedContinents, world.getContinents().size() + newContinents);
    }

    private World createTestWorld() {
        World world = new World(3, 3);
        world.setId(1L);

        WorldSettings worldSettings = TestableWorld.createWorldSettings();
        world.setWorldSettings(worldSettings);

        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setXSize(world.getXSize());
        worldMetaData.setYSize(world.getYSize());
        worldMetaData.setWorld(world);
        worldMetaData.setId(world.getId());
        world.setWorldMetaData(worldMetaData);

        Continent continentX = new Continent(world, SurfaceType.PLAIN);
        continentX.setDirection(new Direction(0, 0));
        Continent continentY = new Continent(world, SurfaceType.OCEAN);
        continentY.setDirection(new Direction(1, 0));
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
                    if (coordinate.getXCoord() + coordinate.getYCoord() < 4) {
                        coordinate.getTile().setHeight(8);
                        coordinate.changeContinent(x);
                        x.getCoordinates().add(coordinate);
                    } else if (coordinate.getYCoord() == 3 && coordinate.getXCoord() == 3) {
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