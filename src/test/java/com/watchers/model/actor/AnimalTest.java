package com.watchers.model.actor;

import com.watchers.model.actor.animals.Rabbit;
import com.watchers.model.environment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {
    private World rabbitWorld;
    private Tile startingTile;
    private Continent rabbitContinent;
    private Rabbit rabbit;

    @BeforeEach
    @SuppressWarnings("all")
    void setRabbit(){
        rabbitWorld = new World(3, 3);
        rabbitContinent = new Continent(rabbitWorld, SurfaceType.CONTINENTAL);

        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                Tile tile = new Tile(x, y, rabbitWorld, rabbitContinent);
                tile.setBiome(new Biome(2f, 3f, 1f, tile));
                rabbitWorld.getTiles().add(tile);
            }
        }

        startingTile = rabbitWorld.getTiles().stream().findFirst().get();

        rabbit = new Rabbit(startingTile, 1f);
        startingTile.getActors().add(rabbit);
    }


    @Test
    void processTurn(){
        assertEquals(1f, rabbit.getFoodReserve());
        rabbit.processSerialTask();
        rabbitWorld.getTiles().forEach(tile -> tile.getBiome().processParallelTask());
        assertEquals(1.5f, rabbit.getFoodReserve());
        rabbitWorld.getTiles().forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(2.0f, rabbit.getFoodReserve());
        rabbitWorld.getTiles().forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(2.5f, rabbit.getFoodReserve());
        rabbitWorld.getTiles().forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(3.0f, rabbit.getFoodReserve());
        rabbitWorld.getTiles().forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(3.5f, rabbit.getFoodReserve());
        rabbitWorld.getTiles().forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(2.0f, rabbit.getFoodReserve());
        rabbitWorld.getTiles().forEach(tile -> tile.getBiome().processParallelTask());
        assertEquals(2, startingTile.getActors().size());
        startingTile.getActors().forEach(Actor::processSerialTask);
        startingTile.getActors().forEach(Actor::processSerialTask);
        assertEquals(1, startingTile.getActors().size());
    }

    @Test
    void processTurnUntillDead(){
        while (rabbit.getStateType()!= StateType.DEAD) {
            rabbit.processSerialTask();
        }

        assertEquals(StateType.DEAD,rabbit.getStateType());
        assertEquals(0f, rabbit.getFoodReserve());
    }

}