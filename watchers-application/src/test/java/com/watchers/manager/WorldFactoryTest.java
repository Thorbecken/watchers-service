package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.config.SettingConfiguration;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;


class WorldFactoryTest {

    private WorldRepositoryInMemory worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);

    @ParameterizedTest
    @CsvSource({"12,13,2","52,28,3"})
    void generateWorldTest(long xSize, long ySize, long continents) {
        SettingConfiguration settingConfiguration = TestableWorld.createConfiguration();
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60, worldRepositoryInMemory);
        World world = new WorldFactory(tileDefined, settingConfiguration).generateWorld(xSize, ySize, continents);

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