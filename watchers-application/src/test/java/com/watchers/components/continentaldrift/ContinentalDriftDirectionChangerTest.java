package com.watchers.components.continentaldrift;

import com.watchers.TestableContinentalDriftTaskDto;
import com.watchers.TestableWorld;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ContinentalDriftDirectionChangerTest {

    private World world;
    private ContinentalDriftDirectionChanger.ContinentalDriftDirectionMethodObject methodObject;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    private final WorldRepository worldRepository = Mockito.mock(WorldRepository.class);
    private long lastContinentalDrift;
    private Set<Continent> continents;
    private ContinentalDriftTaskDto taskDto;

    @BeforeEach
    public void setup(){
        world = TestableWorld.createWorld();
        continents = world.getContinents();

        continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepository);

        taskDto = TestableContinentalDriftTaskDto.createContinentalDriftTaskDto(world);
        lastContinentalDrift = world.getContinents().size()-1;
        methodObject = new ContinentalDriftDirectionChanger.ContinentalDriftDirectionMethodObject(false, lastContinentalDrift);
    }

    @ParameterizedTest
    @CsvSource({"1,1", "2, 2"})
    void changeContinentalDriftDirections(int driftFlux, int driftVelocity) {
        methodObject.adjustContinentelDriftFlux(world, driftFlux, driftVelocity);
        assertThat(world.getLastContinentInFlux(), equalTo(driftFlux-1L));
    }

    @Test
    void changeContinentalDriftDirections() {
        methodObject.adjustContinentelDriftFlux(world, world.getContinents().size(), 1);
        assertThat(world.getLastContinentInFlux(), equalTo(lastContinentalDrift));
    }

    @Test
    void callAdjustForDriftPressureTest() {
        List<Continent> continents = new ArrayList<>(world.getContinents());
        world.getWorldSettings().setDrifFlux(0);
        assertThat(continents, hasSize(3));

        assertThat(continents.get(0).getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continents.get(0).getDirection().getYDriftPressure(), equalTo(0L));
        assertThat(continents.get(1).getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continents.get(1).getDirection().getYDriftPressure(), equalTo(0L));
        assertThat(continents.get(2).getDirection().getXDriftPressure(), equalTo(0L));
        assertThat(continents.get(2).getDirection().getYDriftPressure(), equalTo(0L));

        assertThat(continents.get(0).getDirection().getXVelocity(), equalTo(0));
        assertThat(continents.get(0).getDirection().getYVelocity(), equalTo(-1));
        assertThat(continents.get(1).getDirection().getXVelocity(), equalTo(1));
        assertThat(continents.get(1).getDirection().getYVelocity(), equalTo(0));
        assertThat(continents.get(2).getDirection().getXVelocity(), equalTo(0));
        assertThat(continents.get(2).getDirection().getYVelocity(), equalTo(0));

        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftDirectionChanger.process(taskDto);

        assertThat(continents.get(0).getDirection().getXVelocity(), equalTo(0));
        assertThat(continents.get(0).getDirection().getYVelocity(), equalTo(-1));
        assertThat(continents.get(1).getDirection().getXVelocity(), equalTo(1));
        assertThat(continents.get(1).getDirection().getYVelocity(), equalTo(0));
        assertThat(continents.get(2).getDirection().getXVelocity(), equalTo(0));
        assertThat(continents.get(2).getDirection().getYVelocity(), equalTo(0));

        continents.get(0).getDirection().setXDriftPressure(1);
        continents.get(0).getDirection().setYDriftPressure(1);
        continents.get(1).getDirection().setXDriftPressure(-1);
        continents.get(1).getDirection().setYDriftPressure(-1);
        Mockito.when(worldRepository.findById(taskDto.getWorldId())).thenReturn(Optional.of(world));
        continentalDriftDirectionChanger.process(taskDto);

        assertThat(continents.get(0).getDirection().getXVelocity(), equalTo(1));
        assertThat(continents.get(0).getDirection().getYVelocity(), equalTo(0));
        assertThat(continents.get(1).getDirection().getXVelocity(), equalTo(0));
        assertThat(continents.get(1).getDirection().getYVelocity(), equalTo(-1));
        assertThat(continents.get(2).getDirection().getXVelocity(), equalTo(0));
        assertThat(continents.get(2).getDirection().getYVelocity(), equalTo(0));
    }

    @Test
    void assignFirstOrNewDriftDirections() {
        continents.forEach(continent -> continent.setDirection(null));
        continents.forEach(continent -> assertThat("Continental direction was already assigned!", continent.getDirection(), nullValue()));
        continentalDriftDirectionChanger.assignFirstOrNewDriftDirections(world);
        continents.forEach(continent -> assertThat("Continent wasn't assigned a direction!", continent.getDirection(), notNullValue()));
    }

}