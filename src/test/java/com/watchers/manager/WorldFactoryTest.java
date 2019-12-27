package com.watchers.manager;

import com.watchers.model.environment.World;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;


class WorldFactoryTest {

    @ParameterizedTest
    @CsvSource({"12,13,2","52,28,3"})
    void generateWorldTest(long xSize, long ySize, long conitinents) {
        World world = new WorldFactory().generateWorld(xSize, ySize, conitinents);

        assertNotNull(world);
        assertEquals(Long.valueOf(xSize), world.getXSize());
        assertEquals(Long.valueOf(ySize), world.getYSize());
        assertEquals((xSize*ySize), world.getTiles().size());
        assertEquals(conitinents, world.getContinents().size());
        assertTrue(world.getContinents().stream().allMatch(
                continent -> continent.getTiles().size() > 0
        ));
    }
}