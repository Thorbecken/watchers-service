package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.config.SettingConfiguration;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import com.watchers.model.coordinate.WorldTypeEnum;
import com.watchers.model.world.World;
import com.watchers.model.worldsetting.WorldSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;


class WorldFactoryTest {

    private WorldRepository worldRepository = Mockito.mock(WorldRepository.class);

    @ParameterizedTest
    @CsvSource({"12,13,2","52,28,3"})
    void generateWorldTest(long xSize, long ySize, long continents) {
        SettingConfiguration settingConfiguration = TestableWorld.createConfiguration();
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60, worldRepository);
        World world = new WorldFactory(tileDefined, settingConfiguration).generateWorld(xSize, ySize, continents);
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60);
        WorldSetting worldSetting = new WorldSetting();
        worldSetting.setWorldTypeEnum(WorldTypeEnum.NON_EUCLIDEAN);
        World world = new WorldFactory(tileDefined, settingConfiguration).generateWorld(xSize, ySize, continents, worldSetting);
        world.setWorldSetting(new WorldSetting());
        world.getWorldSetting().setWorldTypeEnum(WorldTypeEnum.NON_EUCLIDEAN);

        Assertions.assertNotNull(world);
        assertEquals(Long.valueOf(xSize), world.getXSize());
        assertEquals(Long.valueOf(ySize), world.getYSize());
        assertEquals((xSize*ySize), world.getCoordinates().size());
        assertEquals(continents, world.getContinents().size());
        assertTrue(world.getContinents().stream().allMatch(
                continent -> continent.getCoordinates().size() > 0
        ));
    }
}