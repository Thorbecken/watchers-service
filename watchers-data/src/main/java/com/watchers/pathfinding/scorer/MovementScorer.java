package com.watchers.pathfinding.scorer;


import com.watchers.model.environment.Tile;

public class MovementScorer implements Scorer<Tile> {
    @Override
    public double computeCost(Tile from, Tile to) {
        return from.getDistance(to);
    }

}