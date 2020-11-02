package com.watchers.components.continentaldrift;

import com.watchers.TestableWorld;
import com.watchers.config.SettingConfiguration;
import com.watchers.model.environment.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Set;

class ContinentalDriftDirectionChangerTest {

    private World world;
    private ContinentalDriftDirectionChanger.ContinentalDriftDirectionMethodObject methodObject;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    private WorldRepositoryInMemory worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);
    private long lastContinentalDrift;
    private Set<Continent> continents;

    @BeforeEach
    public void setup(){
        world = TestableWorld.createWorld();
        continents = world.getContinents();
        SettingConfiguration settingConfiguration = TestableWorld.createConfiguration();

        continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepositoryInMemory, settingConfiguration);

        lastContinentalDrift = world.getContinents().size()-1;
        methodObject = continentalDriftDirectionChanger.new ContinentalDriftDirectionMethodObject(false,lastContinentalDrift);
    }

    @ParameterizedTest
    @CsvSource({"1,1", "2, 2"})
    void changeContinentalDriftDirections(int driftFlux, int driftVelocity) {
        methodObject.adjustContinentelDriftFlux(world, driftFlux, driftVelocity);
        Assert.assertEquals(driftFlux-1, world.getLastContinentInFlux());
    }

    @Test
    void changeContinentalDriftDirections() {
        methodObject.adjustContinentelDriftFlux(world, world.getContinents().size(), 1);
        Assert.assertEquals(lastContinentalDrift, world.getLastContinentInFlux());
    }

    @Test
    void assignFirstOrNewDriftDirections() {
        continents.forEach(continent -> continent.setDirection(null));
        continents.forEach(continent -> Assert.assertNull("Continental direction was already assigned!", continent.getDirection()));
        continentalDriftDirectionChanger.assignFirstOrNewDriftDirections(world);
        continents.forEach(continent -> Assert.assertNotNull("Continent wasn't assigned a direction!", continent.getDirection()));
    }

}