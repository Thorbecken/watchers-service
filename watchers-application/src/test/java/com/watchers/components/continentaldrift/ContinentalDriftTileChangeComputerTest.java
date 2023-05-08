package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.model.common.Direction;
import com.watchers.model.dto.ContinentalChangesDto;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.MockTile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContinentalDriftTileChangeComputerTest {

    private World world;
    private Set<Continent> continents;
    private Continent continentOne;
    private Continent continentTwo;
    private Continent continentThree;
    private ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer;
    private ContinentalDriftTaskDto taskDto;


    @BeforeEach
    void setUp() {
        this.world = TestableWorld.createWorld();
        continents = world.getContinents();
        continentOne = continents.stream().filter(continent -> continent.getId() == 1L).findFirst().orElseThrow();
        continentTwo = continents.stream().filter(continent -> continent.getId() == 2L).findFirst().orElseThrow();
        continentThree = continents.stream().filter(continent -> continent.getId() == 3L).findFirst().orElseThrow();

        this.continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter();

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        continentalDriftPredicter.process(taskDto);
    }

    @Test
    void callAdjustPressureFromIncomingDirectionTest() {
        assertThat(continents, hasSize(3));

        Direction directionOne = continentOne.getDirection();
        Direction directionTwo = continentTwo.getDirection();
        Direction directionThree = continentThree.getDirection();

        assertThat(directionOne.getXDriftPressure(), equalTo(0L));
        assertThat(directionOne.getYDriftPressure(), equalTo(0L));
        assertThat(directionTwo.getXDriftPressure(), equalTo(0L));
        assertThat(directionTwo.getYDriftPressure(), equalTo(0L));
        assertThat(directionThree.getXDriftPressure(), equalTo(0L));
        assertThat(directionThree.getYDriftPressure(), equalTo(0L));

        continentalDriftTileChangeComputer.process(taskDto);

        assertThat(directionOne.getXDriftPressure(), equalTo(0L));
        assertThat(directionOne.getYDriftPressure(), equalTo(0L));
        assertThat(directionTwo.getXDriftPressure(), equalTo(2L));
        assertThat(directionTwo.getYDriftPressure(), equalTo(2L));
        assertThat(directionThree.getXDriftPressure(), equalTo(1L));
        assertThat(directionThree.getYDriftPressure(), equalTo(0L));

    }

    @Test
    void processTest() {
        // setup
        taskDto.setHeightLoss(0);
        world.setHeightDeficit(0);

        long startingHeight = taskDto.getNewTileLayout().values().stream()
                .flatMap(Collection::stream)
                .map(MockTile::getHeight)
                .reduce(Long::sum)
                .orElse(0L);
        // testing

        continentalDriftTileChangeComputer.process(taskDto);

        // assertions
        assertEquals(9, taskDto.getChanges().size());

        List<ContinentalChangesDto> changesDtoList = new ArrayList<>(taskDto.getChanges().values());
        long endHeight = changesDtoList.stream()
                .filter(continentalChangesDto -> !continentalChangesDto.isEmpty())
                .map(ContinentalChangesDto::getMockTile)
                .map(MockTile::getHeight)
                .reduce(Long::sum)
                .orElseThrow();
        endHeight += world.getHeightDeficit();

        assertThat(endHeight, equalTo(startingHeight));
    }
}