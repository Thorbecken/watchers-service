package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
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
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper);
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(2,2);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(coordinateHelper);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner();
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(coordinateHelper);

        continentalDriftManager = new ContinentalDriftManager(continentalDriftPredicter, continentalDriftDirectionChanger, continentalDriftTileChangeComputer, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, tileDefined, erosionAdjuster, 2, 8);
    }

    @Test
    void process() {
        continentalDriftManager.process(world);
    }
}