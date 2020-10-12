package com.watchers.manager;

import com.watchers.TestableWorld;
import com.watchers.components.continentaldrift.*;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class ContinentalDriftManagerTest {

    private World world;
    private ContinentalDriftManager continentalDriftManager;
    private WorldRepositoryInMemory worldRepositoryInMemory;

    @BeforeEach
    void setUp() {
        world = TestableWorld.createWorld();
        worldRepositoryInMemory = Mockito.mock(WorldRepositoryInMemory.class);
        CoordinateHelper coordinateHelper = new CoordinateHelper();
        ContinentalDriftPredicter continentalDriftPredicter = new ContinentalDriftPredicter(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftDirectionChanger continentalDriftDirectionChanger = new ContinentalDriftDirectionChanger(2,2, worldRepositoryInMemory);
        ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer = new ContinentalDriftTileChangeComputer(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster = new ContinentalDriftWorldAdjuster(coordinateHelper, worldRepositoryInMemory);
        ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner = new ContinentalDriftNewTileAssigner(continentalDriftDirectionChanger, 300, worldRepositoryInMemory);
        TileDefined tileDefined = new TileDefined(10,20,30,40,50, 60, worldRepositoryInMemory);
        ErosionAdjuster erosionAdjuster = new ErosionAdjuster(coordinateHelper, 10,8, worldRepositoryInMemory);
        ContinentalCorrector continentalCorrector = new ContinentalCorrector(worldRepositoryInMemory);

        Mockito.when(worldRepositoryInMemory.findById(world.getId())).thenReturn(Optional.of(world));
        continentalDriftManager = new ContinentalDriftManager(continentalDriftPredicter, continentalDriftDirectionChanger, continentalDriftTileChangeComputer, continentalDriftWorldAdjuster, continentalDriftNewTileAssigner, continentalCorrector, tileDefined, erosionAdjuster,2, 8);
    }

    @Test
    void process() {
        continentalDriftManager.process(continentalDriftManager.createTask(world.getId()));
    }
}