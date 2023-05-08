package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ContinentalDriftDirectionChangerTest {

    private World world;
    private Continent continentOne;
    private Continent continentTwo;
    private Continent continentThree;
    private ContinentalDriftDirectionChanger.ContinentalDriftDirectionMethodObject methodObject;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    private Long lastContinentalDrift;
    private Set<Continent> continents;
    private ContinentalDriftTaskDto taskDto;

    @BeforeEach
    public void setup(){
        world = TestableWorld.createWorld();
        continents = world.getContinents();
        continentOne = continents.stream().filter(continent -> continent.getId() == 1L).findFirst().orElseThrow();
        continentTwo = continents.stream().filter(continent -> continent.getId() == 2L).findFirst().orElseThrow();
        continentThree = continents.stream().filter(continent -> continent.getId() == 3L).findFirst().orElseThrow();

        continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger();

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        lastContinentalDrift = (long) world.getContinents().size();
        methodObject = new ContinentalDriftDirectionChanger.ContinentalDriftDirectionMethodObject(false, lastContinentalDrift);
    }

    @ParameterizedTest
    @CsvSource({"1,1", "2, 2"})
    void changeContinentalDriftDirections(int driftFlux, int driftVelocity) {
        methodObject.adjustContinentelDriftFlux(world, driftFlux, driftVelocity);
        assertThat(world.getLastContinentInFlux(), equalTo((long)driftFlux));
    }

    @Test
    void changeContinentalDriftDirections() {
        methodObject.adjustContinentelDriftFlux(world, world.getContinents().size(), 1);
        assertThat(world.getLastContinentInFlux(), equalTo(lastContinentalDrift));
    }

    @Test
    void callAdjustForDriftPressureTest() {
        world.getWorldSettings().setDrifFlux(0);
        assertThat(continents, hasSize(3));

        assertThat(continentOne.getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continentOne.getDirection().getYDriftPressure(), equalTo(0L));
        assertThat(continentTwo.getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continentTwo.getDirection().getYDriftPressure(), equalTo(0L));
        assertThat(continentThree.getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continentThree.getDirection().getYDriftPressure(), equalTo(0L));

        assertThat(continentOne.getDirection().getXVelocity(), equalTo(1));
        assertThat(continentOne.getDirection().getYVelocity(), equalTo(0));
        assertThat(continentTwo.getDirection().getXVelocity(), equalTo(0));
        assertThat(continentTwo.getDirection().getYVelocity(), equalTo(-1));
        assertThat(continentThree.getDirection().getXVelocity(), equalTo(0));
        assertThat(continentThree.getDirection().getYVelocity(), equalTo(0));

        continentalDriftDirectionChanger.process(taskDto);

        assertThat(continentOne.getDirection().getXVelocity(), equalTo(1));
        assertThat(continentOne.getDirection().getYVelocity(), equalTo(0));
        assertThat(continentTwo.getDirection().getXVelocity(), equalTo(0));
        assertThat(continentTwo.getDirection().getYVelocity(), equalTo(-1));
        assertThat(continentThree.getDirection().getXVelocity(), equalTo(0));
        assertThat(continentThree.getDirection().getYVelocity(), equalTo(0));

        continentOne.getDirection().setXDriftPressure(-1);
        continentOne.getDirection().setYDriftPressure(-1);
        continentTwo.getDirection().setXDriftPressure(1);
        continentTwo.getDirection().setYDriftPressure(1);

        continentalDriftDirectionChanger.process(taskDto);

        assertThat(continentOne.getDirection().getXVelocity(), equalTo(0));
        assertThat(continentOne.getDirection().getYVelocity(), equalTo(-1));
        assertThat(continentTwo.getDirection().getXVelocity(), equalTo(1));
        assertThat(continentTwo.getDirection().getYVelocity(), equalTo(0));
        assertThat(continentThree.getDirection().getXVelocity(), equalTo(0));
        assertThat(continentThree.getDirection().getYVelocity(), equalTo(0));
    }

    @Test
    void assignFirstOrNewDriftDirections() {
        continents.forEach(continent -> continent.setDirection(null));
        continents.forEach(continent -> assertThat("Continental direction was already assigned!", continent.getDirection(), nullValue()));
        continentalDriftDirectionChanger.assignFirstOrNewDriftDirections(world);
        continents.forEach(continent -> assertThat("Continent wasn't assigned a direction!", continent.getDirection(), notNullValue()));
    }

}