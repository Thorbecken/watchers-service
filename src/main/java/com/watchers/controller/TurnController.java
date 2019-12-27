package com.watchers.controller;

import com.watchers.model.actor.Actor;
import com.watchers.model.actor.StateType;
import com.watchers.model.environment.World;

public class TurnController {

    public void processTurn(World world){
        world.getTiles().parallelStream().forEach(
                worldTile -> worldTile.getBiome().processParallelTask()
        );

        world.getTiles().forEach(
                worldTile -> {
                    worldTile.getActors().forEach(Actor::processSerialTask);
                    worldTile.getActors().removeIf(actor -> actor.getStateType() == StateType.DEAD);
        });
    }
}
