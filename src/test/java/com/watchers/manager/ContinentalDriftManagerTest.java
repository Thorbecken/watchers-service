package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.ContinentalDriftAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftDirectionAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftTileAdjuster;
import com.watchers.components.continentaldrift.ContinentalDriftWorldAdjuster;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContinentalDriftManagerTest {

    private World world;
    private ContinentalDriftManager continentalDriftManager;

    @BeforeEach
    void setUp() {
        world = TestableWorld.createWorld();
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        ContinentalDriftAdjuster continentalDriftAdjuster = new ContinentalDriftAdjuster(coordinateHelper);
        ContinentalDriftDirectionAdjuster continentalDriftDirectionAdjuster = new ContinentalDriftDirectionAdjuster(2,2);
        ContinentalDriftTileAdjuster continentalDriftTileAdjuster = new ContinentalDriftTileAdjuster(coordinateHelper);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper);

        continentalDriftManager = new ContinentalDriftManager(continentalDriftAdjuster, continentalDriftDirectionAdjuster, continentalDriftTileAdjuster, continentalDriftWorldAdjuster, 2);
    }

    @Test
    void process() {
        continentalDriftManager.process(world);
    }
}