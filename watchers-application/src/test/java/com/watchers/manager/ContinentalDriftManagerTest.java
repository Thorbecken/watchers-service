package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.WorldMetaDataRepository;
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
        WorldMetaDataRepository worldMetaDataRepository = Mockito.mock(WorldMetaDataRepository.class);
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(worldRepository);
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepository);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(worldRepository);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(worldRepository);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(worldRepository, continentalDriftDirectionChanger);
        SurfaceTypeComputator surfaceTypeComputator = new SurfaceTypeComputator(20,30,40,50, 60, worldRepository);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(worldRepository);
        ContinentalCorrector continentalCorrector = new ContinentalCorrector(worldRepository);
        WorldSettingManager worldSettingManager = new WorldSettingManager(worldMetaDataRepository);
        ContinentalIntegretyAdjuster continentalIntegretyAdjuster = new ContinentalIntegretyAdjuster(worldRepository);
        ContinentalSplitter continentalSplitter = new ContinentalSplitter(worldRepository);
        ContinentalMerger continentalMerger = new ContinentalMerger(worldRepository);

        Mockito.when(worldRepository.findById(world.getId())).thenReturn(Optional.of(world));
        continentalDriftManager = new ContinentalDriftManager(continentalDriftPredicter, continentalDriftTileChangeComputer, continentalDriftDirectionChanger, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, continentalCorrector, surfaceTypeComputator, erosionAdjuster, worldSettingManager, continentalIntegretyAdjuster, continentalSplitter, continentalMerger);
    }

    @Test
    void process() {
        continentalDriftManager.process(new ContinentalDriftTaskDto(world.getId(), false, true));
    }
}