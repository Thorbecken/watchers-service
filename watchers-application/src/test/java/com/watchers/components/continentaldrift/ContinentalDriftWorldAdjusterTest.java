package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContinentalDriftWorldAdjusterTest {

    private World world;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        this.world = TestableWorld.createWorld();
        world.getWorldSettings().setContinentalContinentWeight(1);

        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setId(1L);
        worldMetaData.setWorld(world);
        worldMetaData.setXSize(world.getXSize());
        worldMetaData.setYSize(world.getYSize());
        world.setWorldMetaData(worldMetaData);

        this.continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter();
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer();
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(null);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);

        continentalDriftPredicter.process(taskDto);
        continentalDriftTileChangeComputer.process(taskDto);
        continentalDriftNewTileAssigner.process(taskDto);
    }

    @ParameterizedTest
    @CsvSource({"1", "2", "3", "4", "5", "6", "7"})
    void processChanges(int deficit) {
        // altering the heightDeficit because this is calculated in the ContinentalDriftTileChangeComputer,
        // but it is used here
        deficit += world.getHeightDeficit();
        world.setHeightDeficit(deficit);

        // coordinates that have no incoming tiles
        long numberOfNewTilesNeeded = taskDto.getNewTileLayout().values().stream()
                .filter(Collection::isEmpty)
                .count();

        long startingHeight = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                .map(ContinentalChangesDto::getMockTile)
                .map(MockTile::getHeight)
                .reduce(Long::sum)
                .orElseThrow();
        startingHeight += world.getHeightDeficit();

        // testing

        continentalDriftWorldAdjuster.process(taskDto);

//        controlChecks();
        // assertions that all coordinates have a tile
        assertEquals(9, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getTile() != null)
                .count());
        // assertion that all new tiles have at least some height
        // the height varies with the added heightDefecit
        assertTrue(world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .noneMatch(tile -> tile.getHeight() == 0L));

        long endHeight = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getHeight)
                .reduce(Long::sum)
                .orElse(0L);
        endHeight += world.getHeightDeficit();

        Assertions.assertEquals(startingHeight, endHeight);

        long expectedHeightDeficit = deficit % numberOfNewTilesNeeded;
        assertEquals(expectedHeightDeficit, world.getHeightDeficit());
    }
}