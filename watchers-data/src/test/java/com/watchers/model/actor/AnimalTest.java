package com.watchers.model.actor;

import com.watchers.model.actors.Animal;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.actors.Actor;
import com.watchers.model.enums.AnimalType;
import com.watchers.model.enums.StateType;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.*;
import com.watchers.model.world.World;
import com.watchers.model.world.Continent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {
    private World rabbitWorld;
    private Tile startingTile;
    private Animal rabbit;

    @BeforeEach
    @SuppressWarnings("all")
    void setRabbit(){
        rabbitWorld = new World(3, 3);
        Continent rabbitContinent = new Continent(rabbitWorld, SurfaceType.PLAIN);

        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                Coordinate coordinate = CoordinateFactory.createCoordinate(x, y, rabbitWorld, rabbitContinent);
                coordinate.getTile().setBiome(new Biome(coordinate.getTile()));
                coordinate.getTile().setLandMoisture(100d);
                coordinate.getTile().getBiome().setGrassFlora(Flora.GRASS);
                rabbitWorld.getCoordinates().add(coordinate);
            }
        }

        startingTile = rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).findFirst().get();

        rabbit = new Animal(startingTile.getCoordinate(), AnimalType.RABBIT, 1f);
        startingTile.getCoordinate().getActors().add(rabbit);
    }


    @Test
    void processTurn(){
        assertEquals(1f, rabbit.getFoodReserve());
        rabbit.processSerialTask();
        rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.getBiome().processParallelTask());
        assertEquals(1.5f, rabbit.getFoodReserve());
        rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(2.0f, rabbit.getFoodReserve());
        rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(2.5f, rabbit.getFoodReserve());
        rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(3.0f, rabbit.getFoodReserve());
        rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(3.5f, rabbit.getFoodReserve());
        rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.getBiome().processParallelTask());
        rabbit.processSerialTask();
        assertEquals(2.0f, rabbit.getFoodReserve());
        rabbitWorld.getCoordinates().stream().map(Coordinate::getTile).forEach(tile -> tile.getBiome().processParallelTask());
        assertEquals(2, startingTile.getCoordinate().getActors().size());
        startingTile.getCoordinate().getActors().forEach(Actor::processSerialTask);
        startingTile.getCoordinate().getActors().forEach(Actor::processSerialTask);
        assertEquals(1, startingTile.getCoordinate().getActors().size());
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