package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContinentalDriftTileChangeComputerTest {

    private World world;
    private ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer;
    private final WorldRepository worldRepository = Mockito.mock(WorldRepository.class);
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        this.world = TestableWorld.createWorld();
        this.continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(worldRepository);
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(worldRepository);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftPredicter.process(taskDto);
    }

    @Test
    void callAdjustPressureFromIncomingDirectionTest() {
        List<Continent> continents = new ArrayList<>(world.getContinents());
        assertThat(continents, hasSize(3));

        assertThat(continents.get(0).getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continents.get(0).getDirection().getYDriftPressure(), equalTo(0L));
        assertThat(continents.get(1).getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continents.get(1).getDirection().getYDriftPressure(), equalTo(0L));
        assertThat(continents.get(2).getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continents.get(2).getDirection().getYDriftPressure(), equalTo(0L));

        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftTileChangeComputer.process(taskDto);

        assertThat(continents.get(0).getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continents.get(0).getDirection().getYDriftPressure(), equalTo(0L));
        assertThat(continents.get(1).getDirection().getXDriftPressure(), equalTo(2L));
        assertThat(continents.get(1).getDirection().getYDriftPressure(), equalTo(2L));
        assertThat(continents.get(2).getDirection().getXDriftPressure(), equalTo(1L));
        assertThat(continents.get(2).getDirection().getYDriftPressure(), equalTo(0L));

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
                    List<Tile> list = new ArrayList<>();
                    list.addAll(x);
                    list.addAll(y);
                    return list;
                }).stream()
                .flatMap(Collection::stream)
                .map(Tile::getHeight)
                .reduce(Long::sum)
                .orElse(0L);
        // testing

        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftTileChangeComputer.process(taskDto);

        // assertions
        assertEquals(9, taskDto.getChanges().size());

        long endHeight = taskDto.getChanges().values().stream()
                .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                .map(ContinentalChangesDto::getMockTile)
                .map(MockTile::getHeight)
                .reduce(Long::sum)
                .orElseThrow();
        endHeight += world.getHeightDeficit();

        assertThat(endHeight, equalTo(startingHeight));
    }
}