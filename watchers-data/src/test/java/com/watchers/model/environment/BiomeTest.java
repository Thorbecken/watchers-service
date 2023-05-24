package com.watchers.model.environment;

import com.watchers.TestableWorld;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    }

    @Test
    void processParallelTaskGrass() {
        biome.getTile().setLandMoisture(10d);
        biome.setGrassFlora(Flora.GRASS);
        biome.processParallelTask();
        assertThat(biome.getGrassBiomass(), equalTo(1d + Flora.GRASS.getGrowthRate()));
        assertThat(biome.getTreeBiomass(), equalTo(0d));
        assertThat(biome.getCurrentFood(), equalTo(1d + Flora.GRASS.getGrowthRate()));
    }

    @Test
    void processParallelTaskTree() {
        biome.getTile().setLandMoisture(10d);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.processParallelTask();
        assertThat(biome.getGrassBiomass(), equalTo(0d));
        assertThat(biome.getTreeBiomass(), equalTo(1d + Flora.LEAF_TREE.getGrowthRate()));
        assertThat(biome.getCurrentFood(), equalTo(1d + Flora.LEAF_TREE.getGrowthRate()));
    }

    @Test
    void processParallelTaskFullFlora() {
        biome.getTile().setLandMoisture(10d);
        biome.setGrassFlora(Flora.GRASS);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.processParallelTask();
        assertThat(biome.getGrassBiomass(), equalTo(1d + Flora.GRASS.getGrowthRate()));
        assertThat(biome.getTreeBiomass(), equalTo(1d + Flora.LEAF_TREE.getGrowthRate()));
        assertThat(biome.getCurrentFood(), equalTo(2d + Flora.GRASS.getGrowthRate() + Flora.LEAF_TREE.getGrowthRate()));
    }

    @Test
    void processParallelTaskGrassDrought() {
        biome.getTile().setLandMoisture(0.25d);
        biome.setGrassFlora(Flora.GRASS);
        biome.processParallelTask();
        assertThat(biome.getGrassFlora(), notNullValue());
        assertThat(biome.getGrassBiomass(), equalTo(1d));
        assertThat(biome.getCurrentFood(), equalTo(1d));
    }

    @Test
    void processParallelTaskTreeDrought() {
        biome.getTile().setLandMoisture(0.5d);
        biome.setTreeFlora(Flora.LEAF_TREE);
        biome.processParallelTask();
        assertThat(biome.getTreeFlora(), notNullValue());
        assertThat(biome.getTreeBiomass(), equalTo(1d));
        assertThat(biome.getCurrentFood(), equalTo(1d));
    }

    @Test
    void processParallelTaskSevereDrought() {
        biome.getTile().setLandMoisture(0d);
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
        testableWorld.getCoordinates().forEach(coordinate -> coordinate.getTile().setLandMoisture(3d));

        boolean noFloraPresent = testableWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .allMatch(biome1 -> biome1.getTreeFlora() == null && biome1.getGrassFlora() == null);
        assertThat(noFloraPresent, equalTo(true));

        Coordinate startingTile = testableWorld.getCoordinate(2, 2);
        Biome dryBiome = testableWorld.getCoordinate(2, 1).getTile().getBiome();
        dryBiome.getTile().setLandMoisture(0d);
        Biome waterBiome = testableWorld.getCoordinate(2, 3).getTile().getBiome();
        waterBiome.getTile().setSurfaceType(SurfaceType.COASTAL);
        Biome neighbourBiome1 = testableWorld.getCoordinate(1, 2).getTile().getBiome();
        Biome neighbourBiome2 = testableWorld.getCoordinate(3, 2).getTile().getBiome();

        Biome startingBiome = startingTile.getTile().getBiome();
        startingBiome.setGrassFlora(Flora.GRASS);
        startingBiome.setTreeFlora(Flora.LEAF_TREE);
        assertThat(startingBiome.hasOpenFloraSpots(), equalTo(false));

        startingBiome.spread();

        assertThat(neighbourBiome1.hasOpenFloraSpots(), equalTo(false));
        assertThat(neighbourBiome2.hasOpenFloraSpots(), equalTo(false));
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