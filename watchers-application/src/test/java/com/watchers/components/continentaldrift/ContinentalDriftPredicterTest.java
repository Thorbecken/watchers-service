package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class ContinentalDriftPredicterTest {

    private World world;
    private ContinentalDriftPredicter continentalDriftPredicter;
    private ContinentalDriftTaskDto taskDto;
    private WorldRepository worldRepository;


    @BeforeEach
    void setUp() {
        world = TestableWorld.createWorld();
        worldRepository = Mockito.mock(WorldRepository.class);
        continentalDriftPredicter = new ContinentalDriftPredicter(worldRepository);

        Mockito.when(worldRepository.findById(world.getId())).thenReturn(Optional.of(world));
        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
    }

    @Test
    void calculateContinentalDrift() {
        assertEquals(9, world.getCoordinates().stream().filter(coordinate -> coordinate.getTile() != null).count());
        long startingHeight = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getHeight)
                .reduce(Long::sum)
                .orElse(0L);

        taskDto.setNewTileLayout(new HashMap<>());

        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftPredicter.process(taskDto);

        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        assertThat("New continentalLayout was still empty", !newTileLayout.isEmpty(), is(true));
        int count = taskDto.getNewTileLayout().values().stream()
                .map(List::size)
                .reduce(Integer::sum)
                .orElse(0);
        Assertions.assertEquals(9, count);

        long endHeight = taskDto.getNewTileLayout().values().stream()
                .reduce((List<Tile> x, List<Tile> y) ->
                {
                    List<Tile> list = new ArrayList<>();
                    list.addAll(x);
                    list.addAll(y);
                    return list;
                }).stream()
                .flatMap(x -> x.stream())
                .map(Tile::getHeight)
                .reduce(Long::sum)
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
                .reduce(Long::sum)
                .orElse(0L);

        taskDto.setNewTileLayout(new HashMap<>());

        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftPredicter.process(taskDto);

        Map<Coordinate, List<Tile>> newTileLayout = taskDto.getNewTileLayout();

        assertThat("New continentalLayout was still empty", !newTileLayout.isEmpty(), is(true));
        int count = taskDto.getNewTileLayout().values().stream()
                .map(List::size)
                .reduce(Integer::sum)
                .orElse(0);
        Assertions.assertEquals(9, count);

        long endHeight = taskDto.getNewTileLayout().values().stream()
                .reduce((List<Tile> x, List<Tile> y) ->
                {
                    List<Tile> list = new ArrayList<>();
                    list.addAll(x);
                    list.addAll(y);
                    return list;
                }).stream()
                .flatMap(x -> x.stream())
                .map(Tile::getHeight)
                .reduce(Long::sum)
                .orElse(0L);
        Assertions.assertEquals(startingHeight, endHeight);

        assertEquals(9, taskDto.getNewTileLayout().size());
    }
}