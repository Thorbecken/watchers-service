package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
import com.watchers.config.SettingConfiguration;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import com.watchers.repository.WorldSettingsRepository;
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
        WorldRepository worldRepository = Mockito.mock(WorldRepository.class);
        WorldSettingsRepository worldSettingsRepository = Mockito.mock(WorldSettingsRepository.class);
        SettingConfiguration settingConfiguration = TestableWorld.createConfiguration();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(worldRepository);
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepository, settingConfiguration);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(worldRepository);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(worldRepository, settingConfiguration);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(worldRepository, continentalDriftDirectionChanger, settingConfiguration);
        SurfaceTypeComputator surfaceTypeComputator = new SurfaceTypeComputator(20,30,40,50, 60, worldRepository);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(worldRepository, settingConfiguration);
        ContinentalCorrector continentalCorrector = new ContinentalCorrector(worldRepository);
        WorldSettingManager worldSettingManager = new WorldSettingManager(worldSettingsRepository);
        ContinentalIntegretyAdjuster continentalIntegretyAdjuster = new ContinentalIntegretyAdjuster(worldRepository);
        ContinentalSplitter continentalSplitter = new ContinentalSplitter(worldRepository, settingConfiguration);
        ContinentalMerger continentalMerger = new ContinentalMerger(settingConfiguration, worldRepository);

        Mockito.when(worldRepository.findById(world.getId())).thenReturn(Optional.of(world));
        continentalDriftManager = new ContinentalDriftManager(continentalDriftPredicter, continentalDriftTileChangeComputer, continentalDriftDirectionChanger, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, continentalCorrector, surfaceTypeComputator, erosionAdjuster, worldSettingManager, continentalIntegretyAdjuster, continentalSplitter, continentalMerger);
    }

    @Test
    void process() {
        continentalDriftManager.process(new ContinentalDriftTaskDto(world.getId(), false, true, 2, 8));
    }
}