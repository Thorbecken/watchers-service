package com.watchers.model.environment;

import com.watchers.model.common.Direction;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ContinentTest {

    World world;
    Continent continent;

    @BeforeEach
    void setUp() {
        world = new World(1,1);
        world.setLastContinentInFlux(99L);
        continent = new Continent(world, SurfaceType.COASTAL);
        continent.setId(1L);

        Direction direction = new Direction(1,1);
        continent.setDirection(direction);
    }

    @ParameterizedTest
    @CsvSource({"1", "2", "3"})
    void assignNewDriftDirection(int driftVelocity) {
        continent.assignNewDriftDirection(driftVelocity, world);

        Assert.assertTrue("drift out of bound!", (driftVelocity*driftVelocity)>= continent.getDirection().getXVelocity()*continent.getDirection().getXVelocity());
        Assert.assertTrue("drift out of bound!", (driftVelocity*driftVelocity)>= continent.getDirection().getYVelocity()*continent.getDirection().getYVelocity());

        Assert.assertEquals(continent.getId().longValue(), world.getLastContinentInFlux());

    }
}