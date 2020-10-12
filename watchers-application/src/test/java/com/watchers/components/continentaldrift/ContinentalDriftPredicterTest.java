package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ContinentalDriftPredicterTest {

    private World world;
    private ContinentalDriftPredicter continentalDriftPredicter;
    private ContinentalDriftTaskDto taskDto;
    private WorldRepositoryInMemory worldRepositoryInMemory;


    @BeforeEach
    void setUp() {
        world = TestableWorld.createWorld();
        worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper, worldRepositoryInMemory);

        Mockito.when(worldRepositoryInMemory.findById(world.getId())).thenReturn(Optional.of(world));
        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
    }

    @Test
    void calculateContinentalDrift() {
        assertEquals(9, world.getCoordinates().stream().filter(coordinate -> coordinate.getTile() != null).count());
        long startingHeight = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getHeight)
                .reduce((x,y)-> x+y)
                .orElse(0L);

        taskDto.setNewTileLayout(new HashMap<>());

        Mockito.when(worldRepositoryInMemory.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftPredicter.process(taskDto);

        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        Assert.assertTrue("New continentalLayout was still empty", !newTileLayout.isEmpty());
        int count = taskDto.getNewTileLayout().values().stream()
                .map(List::size)
                .reduce((x, y) -> x+y)
                .orElse(0);
        Assertions.assertEquals(9, count);

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
        Assertions.assertEquals(startingHeight, endHeight);

        long zeroCount = newTileLayout.values().stream().filter(x -> x.size() == 0).count();
        Assertions.assertEquals(3, zeroCount);
        long oneCount = newTileLayout.values().stream().filter(x -> x.size() == 1).count();
        Assertions.assertEquals(4, oneCount);
        long twoCount = newTileLayout.values().stream().filter(x -> x.size() == 2).count();
        Assertions.assertEquals(1, twoCount);
        long threeCount = newTileLayout.values().stream().filter(x -> x.size() == 3).count();
        Assertions.assertEquals(1, threeCount);
    }

    @Test
    void calculateContinentalDriftExtreme() {
        ((Continent) world.getContinents().toArray()[0]).getDirection().setXVelocity(2);
        ((Continent) world.getContinents().toArray()[0]).getDirection().setYVelocity(-2);
        ((Continent) world.getContinents().toArray()[1]).getDirection().setXVelocity(-1);
        ((Continent) world.getContinents().toArray()[1]).getDirection().setYVelocity(1);

        assertEquals(9, world.getCoordinates().size());
        long startingHeight = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getHeight)
                .reduce((x,y)-> x+y)
                .orElse(0L);

        taskDto.setNewTileLayout(new HashMap<>());

        Mockito.when(worldRepositoryInMemory.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftPredicter.process(taskDto);

        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        Assert.assertTrue("New continentalLayout was still empty", !newTileLayout.isEmpty());
        int count = taskDto.getNewTileLayout().values().stream()
                .map(List::size)
                .reduce((x, y) -> x+y)
                .orElse(0);
        Assertions.assertEquals(9, count);

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
        Assertions.assertEquals(startingHeight, endHeight);

        assertEquals(9, taskDto.getNewTileLayout().size());
    }
}