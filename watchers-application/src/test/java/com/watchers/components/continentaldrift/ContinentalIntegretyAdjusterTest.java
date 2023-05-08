package com.watchers.components.continentaldrift;

import com.watchers.TestableWorld;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ContinentalIntegretyAdjusterTest {

    private ContinentalIntegretyAdjuster continentalIntegretyAdjuster;
    private WorldRepository worldRepository;

    @BeforeEach
    public void setup(){
        worldRepository = Mockito.mock(WorldRepository.class);
        continentalIntegretyAdjuster = new ContinentalIntegretyAdjuster(worldRepository);
    }

    @Test
    void process() {
        World world = createWorld();
        world.setWorldSettings(TestableWorld.createWorldSettings());
        Continent toBeSplitContinent = world.getContinents().stream()
                .filter(continent -> continent.getId() == 1L)
                .findFirst()
                .orElseThrow();
        Continent fillerContinent = world.getContinents().stream()
                .filter(continent -> continent.getId() == 2L)
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(2, toBeSplitContinent.getCoordinates().size());
        Assertions.assertEquals(13, fillerContinent.getCoordinates().size());

        ContinentalDriftTaskDto continentalDriftTaskDto = new ContinentalDriftTaskDto(world.getWorldMetaData());
        continentalDriftTaskDto.setWorld(world);
        world.getWorldSettings().setHeigtDivider(1);
        world.getWorldSettings().setMinimumContinents(1);
        continentalIntegretyAdjuster.process(continentalDriftTaskDto);

        Assertions.assertEquals(1, toBeSplitContinent.getCoordinates().size());
        Assertions.assertEquals(13, fillerContinent.getCoordinates().size());
        Assertions.assertEquals(3, world.getContinents().size());
        Assertions.assertEquals(15, world.getCoordinates().size());
    }

    private World createWorld() {
        World world = new World();
        world.setXSize(3L);
        world.setYSize(5L);

        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setId(1L);
        worldMetaData.setWorld(world);
        worldMetaData.setXSize(world.getXSize());
        worldMetaData.setYSize(world.getYSize());
        world.setWorldMetaData(worldMetaData);

        Continent toBeSplitContinent = new Continent(world, SurfaceType.OCEAN);
        toBeSplitContinent.setId(1L);
        Continent fillerContinent = new Continent(world, SurfaceType.OCEAN);
        fillerContinent.setId(2L);

        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(1,1, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(1,2, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(1,3, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(1,4, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(1,5, world, fillerContinent));

        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(2,1, world, fillerContinent));
        toBeSplitContinent.getCoordinates().add(CoordinateFactory.createCoordinate(2,2, world, toBeSplitContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(2,3, world, fillerContinent));
        toBeSplitContinent.getCoordinates().add(CoordinateFactory.createCoordinate(2,4, world, toBeSplitContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(2,5, world, fillerContinent));

        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(3,1, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(3,2, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(3,3, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(3,4, world, fillerContinent));
        fillerContinent.getCoordinates().add(CoordinateFactory.createCoordinate(3,5, world, fillerContinent));

        world.getCoordinates().addAll(fillerContinent.getCoordinates());
        world.getCoordinates().addAll(toBeSplitContinent.getCoordinates());

        List<Coordinate> coordinateList = new ArrayList<>(world.getCoordinates());
        for (int i = 0; i < coordinateList.size(); i++) {
            coordinateList.get(i).setId((long) i);
        }

        return world;
    }
}