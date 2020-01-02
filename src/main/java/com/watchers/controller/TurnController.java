package com.watchers.controller;

import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.environment.World;

public class TurnController {

    public static void processTurn(World world){
        world.getConcurrentTiles().parallelStream().forEach(
                worldTile -> worldTile.getBiome().processParallelTask()
        );

        world.getConcurrentTiles().forEach(
                worldTile -> {
                    worldTile.getConcurrentActors().forEach(Actor::processSerialTask);
                    worldTile.getConcurrentActors().removeIf(actor -> actor.getStateType() == StateType.DEAD);
        });
    }
}
