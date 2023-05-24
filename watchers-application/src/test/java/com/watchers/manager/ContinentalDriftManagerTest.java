package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.ContinentRepository;
import com.watchers.repository.WorldMetaDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

class ContinentalDriftManagerTest {

    private World world;
    private ContinentalDriftManager continentalDriftManager;

    @BeforeEach
    void setUp() {
        world = TestableWorld.createWorld();
        ContinentRepository continentRepository = Mockito.mock(ContinentRepository.class);
        WorldMetaDataRepository worldMetaDataRepository = Mockito.mock(WorldMetaDataRepository.class);
        ContinentalMantelPlumeProcessor continentalMantelPlumeProcessor = new ContinentalMantelPlumeProcessor();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter();
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger();
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer();
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster();
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(continentalDriftDirectionChanger);
        SurfaceTypeComputator surfaceTypeComputator = new SurfaceTypeComputator(20, 30, 40, 50, 60);
        ContinentalHotSpotProcessor continentalHotSpotProcessor = new ContinentalHotSpotProcessor();
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster();
        ContinentalCorrector continentalCorrector = new ContinentalCorrector();
        WorldSettingManager worldSettingManager = new WorldSettingManager(worldMetaDataRepository);
        ContinentalIntegretyAdjuster continentalIntegretyAdjuster = new ContinentalIntegretyAdjuster();
        ContinentalSplitter continentalSplitter = new ContinentalSplitter();
        ContinentalMerger continentalMerger = new ContinentalMerger(continentRepository);

        Mockito.when(continentRepository.findAll()).thenReturn(new ArrayList<>(world.getContinents()));
        continentalDriftManager = new ContinentalDriftManager(continentalMantelPlumeProcessor, continentalDriftPredicter, continentalDriftTileChangeComputer, continentalDriftDirectionChanger, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, continentalCorrector, surfaceTypeComputator, continentalHotSpotProcessor, erosionAdjuster, worldSettingManager, continentalIntegretyAdjuster, continentalSplitter, continentalMerger);
    }

    @Test
    void process() {
        ContinentalDriftTaskDto continentalDriftTaskDto = new ContinentalDriftTaskDto(world.getWorldMetaData());
        continentalDriftTaskDto.setContinentalshift(true);
        continentalDriftTaskDto.setWorld(world);
        continentalDriftManager.process(continentalDriftTaskDto);
    }
}