package com.watchers.model.dto;


import com.watchers.model.actors.Actor;
import com.watchers.model.world.Continent;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Tile;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class MockTile {
    private long height;
    private Continent continent;
    private Set<Actor> actorSet = new HashSet<>();
    private SurfaceType surfaceType;
    private float food;

    public MockTile(Tile tile) {
        this.height = tile.getHeight();
        this.continent = tile.getCoordinate().getContinent();
        this.actorSet.addAll(tile.getCoordinate().getActors());
        this.surfaceType = tile.getSurfaceType();
        this.food = tile.getBiome().getCurrentFood();
    }
}
