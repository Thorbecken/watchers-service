package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ContinentalDriftAdjusterTest {

    private ContinentalDriftAdjuster continentalDriftAdjuster;
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        World world = TestableWorld.createWorld();
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        continentalDriftAdjuster = new ContinentalDriftAdjuster(coordinateHelper);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
    }

    @Test
    void calculateContinentalDrift() {
        assertEquals(9, taskDto.getWorld().getTiles().size());
        long startingHeight = taskDto.getWorld().getTiles().stream()
                .map(Tile::getHeight)
                .reduce((x,y)-> x+y)
                .orElse(0L);

        taskDto.setNewTileLayout(new HashMap<>());

        continentalDriftAdjuster.calculateContinentalDrift(taskDto);

        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        Assert.assertTrue("New continentalLayout was still empty", !newTileLayout.isEmpty());
        int count = taskDto.getNewTileLayout().values().stream()
                .map(List::size)
                .reduce((x, y) -> x+y)
                .orElse(0);
        assertEquals(9, count);

        long endHeight = taskDto.getNewTileLayout().values().stream()
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
        assertEquals(startingHeight, endHeight);

        long zeroCount = newTileLayout.values().stream().filter(x -> x.size() == 0).count();
        assertEquals(4, zeroCount);
        long oneCount = newTileLayout.values().stream().filter(x -> x.size() == 1).count();
        assertEquals(2, oneCount);
        long twoCount = newTileLayout.values().stream().filter(x -> x.size() == 2).count();
        assertEquals(2, twoCount);
        long threeCount = newTileLayout.values().stream().filter(x -> x.size() == 3).count();
        assertEquals(1, threeCount);
    }

    @Test
    void calculateContinentalDriftExtreme() {
        ((Continent) taskDto.getWorld().getContinents().toArray()[0]).getDirection().setXVelocity(2);
        ((Continent) taskDto.getWorld().getContinents().toArray()[1]).getDirection().setYVelocity(-2);
        ((Continent) taskDto.getWorld().getContinents().toArray()[0]).getDirection().setXVelocity(-1);
        ((Continent) taskDto.getWorld().getContinents().toArray()[0]).getDirection().setYVelocity(1);

        assertEquals(9, taskDto.getWorld().getTiles().size());
        long startingHeight = taskDto.getWorld().getTiles().stream()
                .map(Tile::getHeight)
                .reduce((x,y)-> x+y)
                .orElse(0L);

        taskDto.setNewTileLayout(new HashMap<>());

        continentalDriftAdjuster.calculateContinentalDrift(taskDto);

        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        Assert.assertTrue("New continentalLayout was still empty", !newTileLayout.isEmpty());
        int count = taskDto.getNewTileLayout().values().stream()
                .map(List::size)
                .reduce((x, y) -> x+y)
                .orElse(0);
        assertEquals(9, count);

        long endHeight = taskDto.getNewTileLayout().values().stream()
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
        assertEquals(startingHeight, endHeight);

        long zeroCount = newTileLayout.values().stream().filter(x -> x.size() == 0).count();
        assertEquals(3, zeroCount);
        long oneCount = newTileLayout.values().stream().filter(x -> x.size() == 1).count();
        assertEquals(5, oneCount);
        long fourCount = newTileLayout.values().stream().filter(x -> x.size() == 4).count();
        assertEquals(1, fourCount);
    }
}