package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
import com.watchers.config.SettingConfiguration;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.World;
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
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper, worldRepository);
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(worldRepository, settingConfiguration);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(coordinateHelper, worldRepository);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper, worldRepository, settingConfiguration);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(worldRepository, continentalDriftDirectionChanger, settingConfiguration);
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60, worldRepository);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(coordinateHelper, worldRepository, settingConfiguration);
        ContinentalCorrector continentalCorrector = new ContinentalCorrector(worldRepository);
        WorldSettingManager worldSettingManager = new WorldSettingManager(worldSettingsRepository);

        Mockito.when(worldRepository.findById(world.getId())).thenReturn(Optional.of(world));
        continentalDriftManager = new ContinentalDriftManager(continentalDriftPredicter, continentalDriftTileChangeComputer, continentalDriftDirectionChanger, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, continentalCorrector, tileDefined, erosionAdjuster, worldSettingManager);
    }

    @Test
    void process() {
        continentalDriftManager.process(new ContinentalDriftTaskDto(world.getId(), false, true, 2, 8));
    }
}