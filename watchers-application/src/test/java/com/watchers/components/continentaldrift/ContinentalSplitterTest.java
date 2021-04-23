package com.watchers.components.continentaldrift;

import com.watchers.TestableWorld;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class ContinentalSplitterTest {



    private World world;
    private WorldRepository worldRepository;
    private ContinentalSplitter continentalSplitter;

    @BeforeEach
    void setUp() {
        worldRepository = Mockito.mock(WorldRepository.class);
        continentalSplitter = new ContinentalSplitter(worldRepository);


        world = new World();
        world.setWorldSettings(TestableWorld.createWorldSettings());
        world.setYSize(5L);
        world.setXSize(3L);
        Continent continent1 = new Continent(world, SurfaceType.OCEAN);
        continent1.setId(1L);
        Continent continent2 = new Continent(world, SurfaceType.OCEAN);
        continent2.setId(2L);
        Continent continent3 = new Continent(world, SurfaceType.OCEAN);
        continent3.setId(3L);
        Continent continent4 = new Continent(world, SurfaceType.OCEAN);
        continent4.setId(4L);

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,1, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,2, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,3, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(1,4, world, continent1));
        continent4.getCoordinates().add(CoordinateFactory.createCoordinate(1,5, world, continent4));

        continent2.getCoordinates().add(CoordinateFactory.createCoordinate(2,1, world, continent2));
        continent2.getCoordinates().add(CoordinateFactory.createCoordinate(2,2, world, continent2));
        continent2.getCoordinates().add(CoordinateFactory.createCoordinate(2,3, world, continent2));
        continent3.getCoordinates().add(CoordinateFactory.createCoordinate(2,4, world, continent3));
        continent4.getCoordinates().add(CoordinateFactory.createCoordinate(2,5, world, continent4));

        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,1, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,2, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,3, world, continent1));
        continent1.getCoordinates().add(CoordinateFactory.createCoordinate(3,4, world, continent1));
        continent4.getCoordinates().add(CoordinateFactory.createCoordinate(3,5, world, continent4));

        world.getCoordinates().addAll(continent1.getCoordinates());
        world.getCoordinates().addAll(continent2.getCoordinates());
        world.getCoordinates().addAll(continent3.getCoordinates());
        world.getCoordinates().addAll(continent4.getCoordinates());
    }

    @Test
    void process() {
        world.getWorldSettings().setMaxWidthLenghtBalance(2);
        world.getWorldSettings().setHeigtDivider(1);
        world.getWorldSettings().setMinimumContinents(1);

        Mockito.when(worldRepository.findById(1L)).thenReturn(Optional.of(world));

        Assertions.assertEquals(8, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent().getId().equals(1L)).count());
        Assertions.assertEquals(3, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent().getId().equals(2L)).count());
        Assertions.assertEquals(1, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent().getId().equals(3L)).count());
        Assertions.assertEquals(3, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent().getId().equals(4L)).count());
        Assertions.assertEquals(4, world.getContinents().size());

        ContinentalDriftTaskDto continentalDriftTaskDto = new ContinentalDriftTaskDto(1L, false, false);
        continentalSplitter.process(continentalDriftTaskDto);

        Assertions.assertEquals(8, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent() != null && coordinate.getContinent().getId() != null)
                .filter(coordinate -> coordinate.getContinent().getId().equals(1L)).count());
        Assertions.assertNotEquals(3, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent() != null && coordinate.getContinent().getId() != null)
                .filter(coordinate -> coordinate.getContinent().getId().equals(2L)).count());
        Assertions.assertEquals(1, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent() != null && coordinate.getContinent().getId() != null)
                .filter(coordinate -> coordinate.getContinent().getId().equals(3L)).count());
        Assertions.assertNotEquals(3, world.getCoordinates().stream()
                .filter(coordinate -> coordinate.getContinent() != null && coordinate.getContinent().getId() != null)
                .filter(coordinate -> coordinate.getContinent().getId().equals(4L)).count());
        Assertions.assertEquals(6, world.getContinents().size());
    }
}