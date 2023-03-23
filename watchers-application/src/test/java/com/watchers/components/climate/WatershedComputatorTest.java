package com.watchers.components.climate;

import com.watchers.TestableWorld;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class WatershedComputatorTest {

    ContinentalDriftTaskDto continentalDriftTaskDto;
    WatershedComputator watershedComputator;
    WorldRepository worldRepository;
    World world;

    @BeforeEach
    void setUp(){
        worldRepository = Mockito.mock(WorldRepository.class);
        watershedComputator = new WatershedComputator(worldRepository);

        world = TestableWorld.createMediumWorld();
        continentalDriftTaskDto = new ContinentalDriftTaskDto(world.getWorldMetaData());
        continentalDriftTaskDto.setWorldId(world.getId());

        world.getCoordinates().forEach(coordinate -> {
            coordinate.getTile().setHeight(50);
            coordinate.getTile().setSurfaceType(SurfaceType.HILL);
        });

        Mockito.when(worldRepository.getById(1L)).thenReturn(world);
    }

    @Test
    void processLowerNeighbours() {
        Coordinate coordinate_start = world.getCoordinate(2, 2);
        coordinate_start.getTile().setHeight(40);
        Coordinate coordinate_same_height = world.getCoordinate(3, 2);
        coordinate_same_height.getTile().setHeight(40);
        Coordinate coordinate_lower_height = world.getCoordinate(2, 3);
        coordinate_lower_height.getTile().setHeight(39);
        Coordinate coordinate_higher = world.getCoordinate(1,2);

        watershedComputator.process(continentalDriftTaskDto);
        boolean allTilesHaveNoWatershed = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .allMatch(tile -> tile.getWatershed() == null);
        assertThat(allTilesHaveNoWatershed, equalTo(true));

        coordinate_start.getTile().setLandMoisture(1);
        watershedComputator.process(continentalDriftTaskDto);
        allTilesHaveNoWatershed = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .allMatch(tile -> tile.getWatershed() == null);
        assertThat(allTilesHaveNoWatershed, equalTo(false));
//      is now filled with a watershed because of moisture
        assertThat(coordinate_start.getTile().getWatershed(), notNullValue());
//      these watersheds still have no watershed because they received no moisture
        assertThat(coordinate_lower_height.getTile().getWatershed(), nullValue());
        assertThat(coordinate_higher.getTile().getWatershed(), nullValue());
        assertThat(coordinate_same_height.getTile().getWatershed(), nullValue());

        coordinate_lower_height.getTile().setLandMoisture(1);
        watershedComputator.process(continentalDriftTaskDto);
        allTilesHaveNoWatershed = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .allMatch(tile -> tile.getWatershed() == null);
        assertThat(allTilesHaveNoWatershed, equalTo(false));
        assertThat(coordinate_start.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_lower_height.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_higher.getTile().getWatershed(), nullValue());
        assertThat(coordinate_same_height.getTile().getWatershed(), nullValue());
        // these tiles should not have the same watershed as the watershed that is above it.
//        assertThat(coordinate_lower_height.getTile().getWatershed(), equalTo(coordinate_start.getTile().getWatershed()));

        coordinate_higher.getTile().setLandMoisture(1);
        watershedComputator.process(continentalDriftTaskDto);
        allTilesHaveNoWatershed = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .allMatch(tile -> tile.getWatershed() == null);
        assertThat(allTilesHaveNoWatershed, equalTo(false));
        assertThat(coordinate_start.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_lower_height.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_higher.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_same_height.getTile().getWatershed(), nullValue());
        // this watershed should have the same watershed as the watershed that is below it.
        assertThat(coordinate_higher.getTile().getWatershed(), equalTo(coordinate_start.getTile().getWatershed()));

        coordinate_same_height.getTile().setLandMoisture(1);
        watershedComputator.process(continentalDriftTaskDto);
        allTilesHaveNoWatershed = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .allMatch(tile -> tile.getWatershed() == null);
        assertThat(allTilesHaveNoWatershed, equalTo(false));
        assertThat(coordinate_start.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_lower_height.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_higher.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_same_height.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_same_height.getTile().getWatershed(), equalTo(coordinate_start.getTile().getWatershed()));
    }

    @Test
    void processHigherNeighbour() {
        Coordinate coordinate_mountain = world.getCoordinate(2, 3);
        coordinate_mountain.getTile().setHeight(60);
        Coordinate coordinate_left_side = world.getCoordinate(2, 2);
        coordinate_left_side.getTile().setHeight(40);
        Coordinate coordinate_right_side = world.getCoordinate(2, 4);
        coordinate_right_side.getTile().setHeight(40);

        coordinate_mountain.getTile().setLandMoisture(1);
        watershedComputator.process(continentalDriftTaskDto);
        assertThat(coordinate_mountain.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_left_side.getTile().getWatershed(), nullValue());
        assertThat(coordinate_right_side.getTile().getWatershed(), nullValue());

        coordinate_left_side.getTile().setLandMoisture(1);
        coordinate_right_side.getTile().setLandMoisture(1);
        coordinate_mountain.getTile().getWatershed().setId(1L);
        watershedComputator.process(continentalDriftTaskDto);
        assertThat(coordinate_mountain.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_left_side.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_right_side.getTile().getWatershed(), notNullValue());
        assertThat(coordinate_mountain.getTile().getWatershed(), not(equalTo(coordinate_left_side.getTile().getWatershed())));
        coordinate_left_side.getTile().getWatershed().setId(2L);
        assertThat(coordinate_mountain.getTile().getWatershed(), not(equalTo(coordinate_right_side.getTile().getWatershed())));
        assertThat(coordinate_left_side.getTile().getWatershed(), not(equalTo(coordinate_right_side.getTile().getWatershed())));
    }
}