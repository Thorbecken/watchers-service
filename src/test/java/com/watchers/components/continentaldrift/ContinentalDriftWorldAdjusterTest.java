package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ContinentalDriftWorldAdjusterTest {

    private World world;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        this.world = TestableWorld.createWorld();
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        this.continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper);
        ContinentalDriftAdjuster continentalDriftAdjuster = new ContinentalDriftAdjuster(coordinateHelper);
        ContinentalDriftTileAdjuster continentalDriftTileAdjuster = new ContinentalDriftTileAdjuster(coordinateHelper);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner();

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);

        continentalDriftAdjuster.process(taskDto);
        continentalDriftTileAdjuster.process(taskDto);
        continentalDriftNewTileAssigner.process(taskDto);
    }

    @ParameterizedTest
    @CsvSource({"1","2","3","4","5","6","7"})
    void processChanges(int deficit) {
        //Fixeme
        deficit += world.getHeightDeficit();
        world.setHeightDeficit(deficit);
        long numberOfNewTilesNeeded = taskDto.getWorld().getTiles().size()-taskDto.getNewTileLayout().values().size();

        long startingHeight = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                .map(ContinentalChangesDto::getNewTile)
                .map(Tile::getHeight)
                .reduce((x,y) -> x+y)
                .get();
        startingHeight += world.getHeightDeficit();

        // testing

        continentalDriftWorldAdjuster.process(taskDto);

        // assertions
        assertEquals(9, world.getTiles().size());
        assertTrue(world.getTiles().stream().noneMatch(tile -> tile.getHeight() == 0L));

        long endHeight = world.getTiles().stream()
                .map(Tile::getHeight)
                .reduce((x,y)-> x+y)
                .orElse(0L);
        endHeight += world.getHeightDeficit();

        assertEquals(startingHeight, endHeight);

        long expectedHeightDeficit = deficit%numberOfNewTilesNeeded;
        assertEquals(expectedHeightDeficit, world.getHeightDeficit());
    }
}