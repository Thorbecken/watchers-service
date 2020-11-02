package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContinentalDriftTileChangeComputerTest {

    private World world;
    private ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer;
    private WorldRepositoryInMemory worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        this.world = TestableWorld.createWorld();
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        this.continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper, worldRepositoryInMemory);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        Mockito.when(worldRepositoryInMemory.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftPredicter.process(taskDto);
    }

    @Test
    void processTest() {
        // setup
        taskDto.setHeightLoss(0);
        world.setHeightDeficit(0);
        taskDto.setChanges(new HashMap<>());

        long startingHeight = taskDto.getNewTileLayout().values().stream()
                .reduce((List<Tile> x, List<Tile> y) ->
                {
                    List<Tile> list = new ArrayList();
                    list.addAll(x);
                    list.addAll(y);
                    return list;
                })
                .get()
                .stream()
                .map(Tile::getHeight)
                .reduce((x, y) -> x + y)
                .orElse(0L);
        // testing

        Mockito.when(worldRepositoryInMemory.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftTileChangeComputer.process(taskDto);

        // assertions
        assertEquals(9, taskDto.getChanges().size());

        long endHeight = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                .map(ContinentalChangesDto::getMockTile)
                .map(MockTile::getHeight)
                .reduce((x,y) -> x+y)
                .get();
        endHeight += world.getHeightDeficit();

        Assert.assertEquals(startingHeight, endHeight);
    }
}