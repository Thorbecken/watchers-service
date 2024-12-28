package com.watchers.model.environment;

import com.watchers.TestableWorld;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BiomeTest {

    private Biome biome;

    @BeforeEach
    void biomeSetup() {
        World world = TestableWorld.createWorld();
        biome = world.getCoordinate(2,2).getTile().getBiome();
    }

    @Test
    void processParallelTask() {
        biome.processParallelTask();
        assertThat(biome.getGrassBiomass(), equalTo(0d));
        assertThat(biome.getTreeBiomass(), equalTo(0d));
        assertThat(biome.getCurrentFood(), equalTo(0d));
        assertThat(biome.getWaterDesire(), equalTo(0d));
        assertThat(biome.getTile().getCoordinate().getClimate().getAirMoisture(), equalTo(0d));
    }

    @Test
    void processParallelTaskGrass() {
        biome.getTile().setGroundWater(10d);
        biome.setGrassFlora(Flora.GRASS);
        biome.processParallelTask();
        assertEquals(biome.getGrassBiomass(), Flora.GRASS.getGrowthRate());
        assertEquals(biome.getTreeBiomass(), 0d);
        assertEquals(biome.getCurrentFood(), Flora.GRASS.getGrowthRate());
        assertEquals(biome.getWaterDesire(), biome.getGrassBiomass() * Flora.GRASS.getGrowthRate() * Flora.GRASS.getWaterIntake(), 0.01);
        assertEquals(biome.getTile().getGroundWater(), 10d-Flora.GRASS.getWaterIntake() * Flora.GRASS.getGrowthRate(), 0.01);
        assertEquals(biome.getTile().getCoordinate().getClimate().getAirMoisture(), Flora.GRASS.getWaterIntake() * Flora.GRASS.getGrowthRate(),0.01);
    }

    @Test
    void processParallelTaskTree() {
        biome.getTile().setGroundWater(10d);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.processParallelTask();
        assertEquals(biome.getGrassBiomass(),0d);
        assertEquals(biome.getTreeBiomass(), Flora.LEAF_TREE.getGrowthRate());
        assertEquals(biome.getCurrentFood(), Flora.LEAF_TREE.getGrowthRate());
        assertEquals(biome.getWaterDesire(), biome.getTreeBiomass() * Flora.LEAF_TREE.getGrowthRate() * Flora.LEAF_TREE.getWaterIntake(), 0.01);
        assertEquals(biome.getTile().getGroundWater(), 10d-Flora.LEAF_TREE.getWaterIntake() * Flora.LEAF_TREE.getGrowthRate(),0.01);
        assertEquals(biome.getTile().getCoordinate().getClimate().getAirMoisture(), Flora.LEAF_TREE.getWaterIntake() * Flora.LEAF_TREE.getGrowthRate(), 0.01);
    }

    @Test
    void processParallelTaskFullFlora() {
        biome.getTile().setGroundWater(10d);
        biome.setGrassFlora(Flora.GRASS);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.processParallelTask();
        assertThat(biome.getGrassBiomass(), equalTo(Flora.GRASS.getGrowthRate()));
        assertThat(biome.getTreeBiomass(), equalTo(Flora.LEAF_TREE.getGrowthRate()));
        assertThat(biome.getCurrentFood(), equalTo(Flora.GRASS.getGrowthRate() + Flora.LEAF_TREE.getGrowthRate()));
        assertThat(biome.getWaterDesire(), equalTo((biome.getTreeBiomass() * Flora.LEAF_TREE.getGrowthRate() * Flora.LEAF_TREE.getWaterIntake())+biome.getGrassBiomass() * Flora.GRASS.getGrowthRate() * Flora.GRASS.getWaterIntake()));
        assertThat(biome.getTile().getGroundWater(), equalTo(10d-(Flora.LEAF_TREE.getWaterIntake() * Flora.LEAF_TREE.getGrowthRate())-(Flora.GRASS.getWaterIntake() * Flora.GRASS.getGrowthRate())));
        assertThat(biome.getTile().getCoordinate().getClimate().getAirMoisture(), equalTo(((Flora.LEAF_TREE.getWaterIntake() * Flora.LEAF_TREE.getGrowthRate())+(Flora.GRASS.getWaterIntake() * Flora.GRASS.getGrowthRate()))));
    }

    @Test
    void processParallelTaskGrassDrought() {
        biome.getTile().setGroundWater(0.025d);
        biome.setGrassFlora(Flora.GRASS);
        biome.processParallelTask();
        assertThat(biome.getGrassFlora(), notNullValue());
        assertEquals(biome.getGrassBiomass(), 1d,0.01);
        assertEquals(biome.getCurrentFood(), 1d, 0.01);
    }

    @Test
    void processParallelTaskTreeDrought() {
        biome.getTile().setGroundWater(0.05d);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.processParallelTask();
        assertThat(biome.getTreeFlora(), notNullValue());
        assertEquals(biome.getTreeBiomass(), 1d, 0.01);
        assertEquals(biome.getCurrentFood(), 1d, 0.01);
    }

    @Test
    void processParallelTaskSevereDrought() {
        biome.getTile().setGroundWater(0d);
        biome.setGrassFlora(Flora.GRASS);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.processParallelTask();
        assertThat(biome.getGrassFlora(), nullValue());
        assertThat(biome.getTreeFlora(), nullValue());
        assertThat(biome.getGrassBiomass(), equalTo(0d));
        assertThat(biome.getTreeBiomass(), equalTo(0d));
        assertThat(biome.getCurrentFood(), equalTo(0d));
    }

    @Test
    void spreadTest() {
        World testableWorld = TestableWorld.createWorld();
        testableWorld.getCoordinates().forEach(coordinate -> coordinate.getTile().setSurfaceType(SurfaceType.PLAIN));
        testableWorld.getCoordinates().forEach(coordinate -> coordinate.getTile().setGroundWater(3d));

        boolean noFloraPresent = testableWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .allMatch(biome1 -> biome1.getTreeFlora() == null && biome1.getGrassFlora() == null);
        assertThat(noFloraPresent, equalTo(true));

        Coordinate startingTile = testableWorld.getCoordinate(2, 2);
        Biome dryBiome = testableWorld.getCoordinate(2, 1).getTile().getBiome();
        dryBiome.getTile().setGroundWater(0d);
        Biome waterBiome = testableWorld.getCoordinate(2, 3).getTile().getBiome();
        waterBiome.getTile().setSurfaceType(SurfaceType.COASTAL);
        Biome neighbourBiome1 = testableWorld.getCoordinate(1, 2).getTile().getBiome();
        Biome neighbourBiome2 = testableWorld.getCoordinate(3, 2).getTile().getBiome();

        Biome startingBiome = startingTile.getTile().getBiome();
        startingBiome.setGrassFlora(Flora.GRASS);
        startingBiome.setTreeFlora(Flora.LEAF_TREE);
        assertThat(startingBiome.hasOpenFloraSpots(), equalTo(false));

        startingBiome.spread();

        assertThat(neighbourBiome1.hasOpenFloraSpots(), equalTo(true));
        assertThat(neighbourBiome2.hasOpenFloraSpots(), equalTo(true));
        assertThat(dryBiome.hasOpenFloraSpots(), equalTo(true));
        assertThat(waterBiome.hasOpenFloraSpots(), equalTo(true));
    }

    @Test
    void forageTest() {
        biome.setGrassFlora(Flora.GRASS);
        biome.setGrassBiomass(10d);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.setTreeBiomass(10d);
        assertThat(biome.getCurrentFood(), equalTo(20d));

        biome.forage(5d);

        assertThat(biome.getCurrentFood(), equalTo(15d));
        assertThat(biome.getGrassBiomass(), equalTo(5d));
        assertThat(biome.getTreeBiomass(), equalTo(10d));
        assertThat(biome.getGrassFlora(), notNullValue());
        assertThat(biome.getTreeFlora(), notNullValue());

        biome.forage(6d);

        assertThat(biome.getCurrentFood(), equalTo(9d));
        assertThat(biome.getGrassBiomass(), equalTo(0d));
        assertThat(biome.getTreeBiomass(), equalTo(9d));
        assertThat(biome.getGrassFlora(), nullValue());
        assertThat(biome.getTreeFlora(), notNullValue());

        biome.forage(9d);

        assertThat(biome.getCurrentFood(), equalTo(0d));
        assertThat(biome.getGrassBiomass(), equalTo(0d));
        assertThat(biome.getTreeBiomass(), equalTo(0d));
        assertThat(biome.getGrassFlora(), nullValue());
        assertThat(biome.getTreeFlora(), nullValue());
    }

    @Test
    void addBiomassTest(){
        biome.setGrassFlora(Flora.GRASS);
        biome.setGrassBiomass(1d);
        assertThat(biome.getGrassBiomass(), equalTo(1d));
        biome.addGrassBiomass(1d);
        assertThat(biome.getGrassBiomass(), equalTo(2d));
        biome.addGrassBiomass(10d);
        assertThat(biome.getGrassBiomass(), equalTo(10d));

        biome.setTreeFlora(Flora.PINE_TREE);
        biome.setTreeBiomass(1d);
        assertThat(biome.getTreeBiomass(), equalTo(1d));
        biome.addTreeBiomass(1d);
        assertThat(biome.getTreeBiomass(), equalTo(2d));
        biome.addTreeBiomass(30d);
        assertThat(biome.getTreeBiomass(), equalTo(30d));
    }
}