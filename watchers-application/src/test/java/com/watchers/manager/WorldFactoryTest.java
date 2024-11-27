package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorldFactoryTest {

    @ParameterizedTest
    @CsvSource({"12,13,2", "52,28,3", "0,0,0", "100,100,10"})
    void generateWorldTest(long xSize, long ySize, int continents) {
        WorldSettings worldSettings = TestableWorld.createWorldSettings();
        worldSettings.setXSize(xSize);
        worldSettings.setYSize(ySize);
        worldSettings.setNumberOfContinents(continents);
        TileDefined tileDefined = new TileDefined(10, 20, 30, 40, 50, 60);
        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setWorldTypeEnum(WorldTypeEnum.NON_EUCLIDEAN);

        World world = new WorldFactory(tileDefined)
                .generateWorld(worldSettings, worldMetaData);
        world.setWorldMetaData(new WorldMetaData());
        world.getWorldMetaData().setWorldTypeEnum(WorldTypeEnum.NON_EUCLIDEAN);

        Assertions.assertNotNull(world);
        assertEquals(Long.valueOf(xSize), world.getXSize());
        assertEquals(Long.valueOf(ySize), world.getYSize());
        assertEquals((xSize * ySize), world.getCoordinates().size());
        assertEquals(continents, world.getContinents().size());

        assertTrue(world.getContinents().stream().allMatch(
                continent -> continent.getCoordinates().size() > 0
        ));

        // Extra checks for edge cases
        if (xSize > 0 && ySize > 0) {
            assertTrue(world.getCoordinates().stream().allMatch(
                    coordinate -> coordinate.getXCoord() >= 1 && coordinate.getXCoord() <= xSize &&
                            coordinate.getYCoord() >= 1 && coordinate.getYCoord() <= ySize
            ));
        }

        // Check for uniqueness of coordinates if applicable
        Set<Coordinate> uniqueCoordinates = new HashSet<>(world.getCoordinates());
        assertEquals(world.getCoordinates().size(), uniqueCoordinates.size());

        for (Continent continent : world.getContinents()) {
            assertNotEquals(1, continent.getCoordinates().size(), "Continent " + continent.getId() + " has only one Coordinate.");
            List<Set<Coordinate>> landMasses = CoordinateHelper.getListsOfAdjacentCoordinatesFromContinent(continent);

            assertFalse(landMasses.isEmpty(), "Continent " + continent.getId() + " has no landmasses.");

            landMasses.forEach(landMass -> {
                assertTrue(landMass.size() > 0, "A landmass is empty.");
                assertEquals(1, landMasses.size(), "Continent " + continent.getId() + " has more than one landmass.");
            });
        }
    }

}