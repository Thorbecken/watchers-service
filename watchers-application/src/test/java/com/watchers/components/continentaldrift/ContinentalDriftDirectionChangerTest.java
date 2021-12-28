package com.watchers.components.continentaldrift;

import com.watchers.TestableWorld;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

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

    @BeforeEach
    public void setup(){
        world = TestableWorld.createWorld();
        continents = world.getContinents();

        continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepository);

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
    void assignFirstOrNewDriftDirections() {
        continents.forEach(continent -> continent.setDirection(null));
        continents.forEach(continent -> assertThat("Continental direction was already assigned!", continent.getDirection(), nullValue()));
        continentalDriftDirectionChanger.assignFirstOrNewDriftDirections(world);
        continents.forEach(continent -> assertThat("Continent wasn't assigned a direction!", continent.getDirection(), notNullValue()));
    }

}