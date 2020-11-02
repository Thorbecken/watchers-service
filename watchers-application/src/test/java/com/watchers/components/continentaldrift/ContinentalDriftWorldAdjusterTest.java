package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.config.SettingConfiguration;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ContinentalDriftWorldAdjusterTest {

    private World world;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        this.world = TestableWorld.createWorld();
        SettingConfiguration settingConfiguration = TestableWorld.createConfiguration();
        settingConfiguration.setContinentalContinentWeight(1);
        WorldRepositoryInMemory worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        this.continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper, worldRepositoryInMemory, settingConfiguration);
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(worldRepositoryInMemory, null, settingConfiguration);

        Mockito.when(worldRepositoryInMemory.findById(world.getId())).thenReturn(Optional.of(world));
        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);

        continentalDriftPredicter.process(taskDto);
        continentalDriftTileChangeComputer.process(taskDto);
        continentalDriftNewTileAssigner.process(taskDto);
    }

    @ParameterizedTest
    @CsvSource({"1","2","3","4","5","6","7"})
    void processChanges(int deficit) {
        deficit += world.getHeightDeficit();
        world.setHeightDeficit(deficit);
        long numberOfNewTilesNeeded = world.getCoordinates().size()-taskDto.getNewTileLayout().values().stream().filter(tiles ->  tiles.size() > 0).count();

        long startingHeight = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                .map(ContinentalChangesDto::getMockTile)
                .map(MockTile::getHeight)
                .reduce((x,y) -> x+y)
                .get();
        startingHeight += world.getHeightDeficit();

        // testing

        continentalDriftWorldAdjuster.process(taskDto);

        // assertions
        assertEquals(9, world.getCoordinates().stream().filter(coordinate -> coordinate.getTile() != null).count());
        assertTrue(world.getCoordinates().stream().map(Coordinate::getTile).noneMatch(tile -> tile.getHeight() == 0L));

        long endHeight = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getHeight)
                .reduce((x,y)-> x+y)
                .orElse(0L);
        endHeight += world.getHeightDeficit();

        Assertions.assertEquals(startingHeight, endHeight);

        long expectedHeightDeficit = deficit%numberOfNewTilesNeeded;
        assertEquals(expectedHeightDeficit, world.getHeightDeficit());
    }
}