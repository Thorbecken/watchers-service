package com.watchers.components.continentaldrift;

import com.watchers.TestableWorld;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.repository.ContinentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class ContinentalMergerTest {

    private World world;
    private ContinentalMerger continentalMerger;

    @BeforeEach
    void setUp() {
        ContinentRepository continentRepository = mock(ContinentRepository.class);
        continentalMerger = new ContinentalMerger(continentRepository);

        world = new World();
        world.setYSize(5L);
        world.setXSize(3L);
        world.setWorldSettings(TestableWorld.createWorldSettings());

        WorldMetaData worldMetaData = new WorldMetaData();
        worldMetaData.setId(1L);
        worldMetaData.setWorld(world);
        worldMetaData.setXSize(world.getXSize());
        worldMetaData.setYSize(world.getYSize());
        world.setWorldMetaData(worldMetaData);

        Continent continent1 = new Continent(world, SurfaceType.OCEAN);
        continent1.setId(1L);
        Continent continent2 = new Continent(world, SurfaceType.OCEAN);
        continent2.setId(2L);
        Continent continent3 = new Continent(world, SurfaceType.OCEAN);
        continent3.setId(3L);

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1, 1, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1, 2, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1, 3, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1, 4, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1, 5, world, continent1));

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(2, 1, world, continent1));
        continent2.getCoordinates().add(CoordinateFactory.createCoordinate(2, 2, world, continent2));
        continent2.getCoordinates().add(CoordinateFactory.createCoordinate(2, 3, world, continent2));
        continent3.getCoordinates().add(CoordinateFactory.createCoordinate(2, 4, world, continent3));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(2, 5, world, continent1));

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3, 1, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3, 2, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3, 3, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3, 4, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3, 5, world, continent1));

        world.getCoordinates().addAll(continent1.getCoordinates());
        world.getCoordinates().addAll(continent2.getCoordinates());
        world.getCoordinates().addAll(continent3.getCoordinates());
    }

    @Test
    void process() {
        world.getWorldSettings().setMinimumContinents(1);
        world.getWorldSettings().setHeigtDivider(1);
        world.getWorldSettings().setMaximumContinents(2);

        Assertions.assertEquals(3, world.getContinents().size());

        Assertions.assertTrue(world.getContinents().stream().anyMatch(continent -> continent.getId().equals(1L)));
        Assertions.assertTrue(world.getContinents().stream().anyMatch(continent -> continent.getId().equals(2L)));
        Assertions.assertTrue(world.getContinents().stream().anyMatch(continent -> continent.getId().equals(3L)));

        ContinentalDriftTaskDto continentalDriftTaskDto = new ContinentalDriftTaskDto(world.getWorldMetaData());
        continentalDriftTaskDto.setWorld(world);
        continentalMerger.process(continentalDriftTaskDto);

        Assertions.assertEquals(2, world.getContinents().size());

        Assertions.assertTrue(world.getContinents().stream().anyMatch(continent -> continent.getId().equals(1L)));
        Assertions.assertTrue(world.getContinents().stream().anyMatch(continent -> continent.getId().equals(2L)));
        Assertions.assertTrue(world.getContinents().stream().noneMatch(continent -> continent.getId().equals(3L)));
    }
}