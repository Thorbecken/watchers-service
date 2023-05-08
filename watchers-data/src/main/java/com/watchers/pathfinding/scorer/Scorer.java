package com.watchers.pathfinding.scorer;

import com.watchers.pathfinding.GraphNode;

public interface Scorer<T extends GraphNode> {
    double computeCost(T from, T to);
}