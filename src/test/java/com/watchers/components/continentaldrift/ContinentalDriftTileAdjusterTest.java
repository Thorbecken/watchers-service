package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContinentalDriftTileAdjusterTest {

    private World world;
    private ContinentalDriftTileAdjuster continentalDriftTileAdjuster;
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        this.world = TestableWorld.createWorld();
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        this.continentalDriftTileAdjuster = new ContinentalDriftTileAdjuster(coordinateHelper);
        ContinentalDriftAdjuster continentalDriftAdjuster = new ContinentalDriftAdjuster(coordinateHelper);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        continentalDriftAdjuster.calculateContinentalDrift(taskDto);
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

        continentalDriftTileAdjuster.process(taskDto);

        // assertions
        assertEquals(9, taskDto.getChanges().size());

        long endHeight = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                .map(ContinentalChangesDto::getNewTile)
                .map(Tile::getHeight)
                .reduce((x,y) -> x+y)
                .get();
        endHeight += world.getHeightDeficit();

        Assert.assertEquals(startingHeight, endHeight);
    }
}