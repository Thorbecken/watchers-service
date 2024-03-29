package com.watchers.model.dto;


import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Flora;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.base.PointOfInterest;
import lombok.Data;

@Data
public class MockTile {
    private long height;
    private MockCoordinate coordinateOfOrigin;
    private MockContinentObject mockContinentObject;
    private SurfaceType surfaceType;
    private double grassBiomass;
    private Flora grassFlora;
    private double treeBiomass;
    private Flora treeFlora;
    private PointOfInterest pointOfInterest;

    public MockTile(Tile tile) {
        this.height = tile.getHeight();
        this.coordinateOfOrigin = new MockCoordinate(tile.getCoordinate());
        this.mockContinentObject = new MockContinentObject(tile.getCoordinate().getContinent());
        this.surfaceType = tile.getSurfaceType();
        this.grassBiomass = tile.getBiome().getGrassBiomass();
        this.grassFlora = tile.getBiome().getGrassFlora();
        this.treeBiomass = tile.getBiome().getTreeBiomass();
        this.treeFlora = tile.getBiome().getTreeFlora();
        this.pointOfInterest = tile.getPointOfInterest();
    }
}
