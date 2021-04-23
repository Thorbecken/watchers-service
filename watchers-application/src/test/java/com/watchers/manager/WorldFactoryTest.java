package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.model.world.WorldSettings;
import com.watchers.model.world.WorldTypeEnum;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class WorldFactoryTest {

    private final WorldRepository worldRepository = Mockito.mock(WorldRepository.class);

    @ParameterizedTest
    @CsvSource({"12,13,2","52,28,3"})
    void generateWorldTest(long xSize, long ySize, int continents) {
        WorldSettings worldSettings = TestableWorld.createWorldSettings();
        worldSettings.setXSize(xSize);
        worldSettings.setYSize(ySize);
        worldSettings.setNumberOfContinents(continents);
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60, worldRepository);
        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setWorldTypeEnum(WorldTypeEnum.NON_EUCLIDEAN);
        World world = new WorldFactory(tileDefined, null, null, null, null).generateWorld(worldSettings, worldMetaData);
        world.setWorldMetaData(new WorldMetaData());
        world.getWorldMetaData().setWorldTypeEnum(WorldTypeEnum.NON_EUCLIDEAN);

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