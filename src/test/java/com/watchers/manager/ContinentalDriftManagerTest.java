package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.Tile;
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
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner();
        TileDefined tileDefined = new TileDefined(10,20,30,40,50);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(coordinateHelper);

        continentalDriftManager = new ContinentalDriftManager(continentalDriftAdjuster, continentalDriftDirectionAdjuster, continentalDriftTileAdjuster, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, tileDefined, erosionAdjuster, 2, 8);
    }

    @Test
    void process() {
        continentalDriftManager.process(world);
    }
}