package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
import com.watchers.config.SettingConfiguration;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import com.watchers.repository.inmemory.WorldSettingsRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class ContinentalDriftManagerTest {

    private World world;
    private ContinentalDriftManager continentalDriftManager;

    @BeforeEach
    void setUp() {
        world = TestableWorld.createWorld();
        WorldRepositoryInMemory worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);
        WorldSettingsRepositoryInMemory worldSettingsRepositoryInMemory = Mockito.mock(WorldSettingsRepositoryInMemory.class);
        SettingConfiguration settingConfiguration = TestableWorld.createConfiguration();
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepositoryInMemory, settingConfiguration);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper, worldRepositoryInMemory, settingConfiguration);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(worldRepositoryInMemory, continentalDriftDirectionChanger, settingConfiguration);
        SurfaceTypeComputator surfaceTypeComputator = new SurfaceTypeComputator(20,30,40,50, 60, worldRepositoryInMemory);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(coordinateHelper, worldRepositoryInMemory, settingConfiguration);
        ContinentalCorrector continentalCorrector = new ContinentalCorrector(worldRepositoryInMemory);
        WorldSettingManager worldSettingManager = new WorldSettingManager(worldSettingsRepositoryInMemory);

        Mockito.when(worldRepositoryInMemory.findById(world.getId())).thenReturn(Optional.of(world));
        continentalDriftManager = new ContinentalDriftManager(continentalDriftPredicter, continentalDriftTileChangeComputer, continentalDriftDirectionChanger, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, continentalCorrector, surfaceTypeComputator, erosionAdjuster, worldSettingManager);
    }

    @Test
    void process() {
        continentalDriftManager.process(new ContinentalDriftTaskDto(world.getId(), false, true, 2, 8));
    }
}