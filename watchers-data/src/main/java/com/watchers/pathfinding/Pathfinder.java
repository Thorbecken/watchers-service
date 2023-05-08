package com.watchers.pathfinding;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.pathfinding.scorer.DistanceScorer;
import com.watchers.pathfinding.scorer.MovementScorer;

import java.util.*;
import java.util.stream.Collectors;

public class Pathfinder {

    private final RouteFinder<Tile> routeFinder;

    public Pathfinder(World world) {
        Set<Tile> tiles = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .collect(Collectors.toSet());
        Map<Long, Set<Long>> connections = new HashMap<>();

        tiles.forEach(tile -> connections.put(tile.getId(), tile.getNeighbourIds()));

        Graph<Tile> worldMap = new Graph<>(tiles, connections);
        routeFinder = new RouteFinder<>(worldMap, new MovementScorer(), new DistanceScorer());
    }

    public List<Tile> pathfind(Tile from, Tile to) {
        if (from != null && to != null) {
            List<Tile> route = routeFinder.findRoute(from, to);
            // remove the first tile, because that's the starting tile.
            route.remove(0);
            return route;
        } else {
            return new ArrayList<>();
        }
    }
}
