package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.WorldCleanser;
import com.watchers.components.continentaldrift.*;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ContinentalDriftManagerTest {

    private World world;
    private ContinentalDriftManager continentalDriftManager;
    private WorldRepositoryInMemory worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);

    @BeforeEach
    void setUp() {
        world = TestableWorld.createWorld();
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper);
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(2,2);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(coordinateHelper);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(continentalDriftDirectionChanger, 300);
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(coordinateHelper, 10,8);
        WorldCleanser worldCleanser = new WorldCleanser(worldRepositoryInMemory);
        ContinentalCorrector continentalCorrector = new ContinentalCorrector();

        continentalDriftManager = new ContinentalDriftManager(continentalDriftPredicter, continentalDriftDirectionChanger, continentalDriftTileChangeComputer, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, continentalCorrector, tileDefined, erosionAdjuster, worldCleanser, worldRepositoryInMemory,2, 8);
    }

    @Test
    void process() {
        continentalDriftManager.process(world);
    }
}