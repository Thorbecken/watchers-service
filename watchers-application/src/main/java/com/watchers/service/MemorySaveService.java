package com.watchers.service;

import com.mchange.util.AssertException;
import com.watchers.model.actor.Actor;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.inmemory.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@AllArgsConstructor
public class MemorySaveService {

    private WorldRepositoryInMemory worldRepositoryInMemory;
    private ContinentRepositoryInMemory continentRepositoryInMemory;
    private CoordinateRepositoryInMemory coordinateRepositoryInMemory;
    private TileRepositoryInMemory tileRepositoryInMemory;
    private BiomeRepositoryInMemory biomeRepositoryInMemory;
    private ActorRepositoryInMemory actorRepositoryInMemory;

    public void complexSaveToMemory(World persistentWorld) {
        World memoryWorld = new World(persistentWorld.getXSize(), persistentWorld.getYSize());
        memoryWorld.setId(persistentWorld.getId());
        worldRepositoryInMemory.save(memoryWorld);

        saveBasicWorld(memoryWorld, persistentWorld);
        saveContinents(memoryWorld.getId(), persistentWorld);
        saveCoordinates(memoryWorld.getId(), persistentWorld);
        saveTiles(memoryWorld.getId(), persistentWorld);
        saveBiomes(memoryWorld.getId(), persistentWorld);
        saveActors(memoryWorld.getId(), persistentWorld);
    }

    private void saveActors(Long id, World persistentWorld) {
        Assert.isTrue(0 == actorRepositoryInMemory.count(), "Expected 0 but was " + actorRepositoryInMemory.count());

        World newWorld = worldRepositoryInMemory.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(persistentWorld);
        List<Actor> actors = persistentWorld.getActorList().stream()
                .map(actor -> actor.createClone(newWorld.getCoordinate(actor.getCoordinate().getXCoord(), actor.getCoordinate().getYCoord())))
                .collect(Collectors.toList());
        actors.forEach(actor -> actor.getCoordinate().getActors().add(actor));
        log.info(actors.size() + " " + Arrays.toString(actors.stream().map(Actor::getId).toArray()));
        actorRepositoryInMemory.saveAll(actors);
        worldRepositoryInMemory.save(newWorld);
        Iterable<Actor> actorz =  actorRepositoryInMemory.findAll();
        log.info("Current actors in memory: " + StreamSupport.stream(actorz.spliterator(),false)
                .map(Actor::getCoordinate)
                .map(Coordinate::getId)
                .collect(Collectors.toList()).toString());
        Assert.isTrue(persistentWorld.getActorList().size() == actorRepositoryInMemory.count(), "Expected " + persistentWorld.getActorList().size() + " but was " + actorRepositoryInMemory.count());
    }

    private void saveBiomes(Long id, World persistentWorld) {
        World newWorld = worldRepositoryInMemory.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(persistentWorld);
        List<Biome> biomes = persistentWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .map(biome -> biome.createClone(newWorld.getCoordinate(biome.getTile().getCoordinate().getXCoord(), biome.getTile().getCoordinate().getYCoord()).getTile()))
                .collect(Collectors.toList());
        biomes.forEach(biome -> biome.getTile().setBiome(biome));
        log.info("Current biomes in memory: " + biomes.size() + " " + Arrays.toString(biomes.stream().map(Biome::getId).toArray()));
        biomeRepositoryInMemory.saveAll(biomes);
        worldRepositoryInMemory.save(newWorld);
        Assert.isTrue(persistentWorld.getCoordinates().size() == biomeRepositoryInMemory.count(), "Expected " + persistentWorld.getCoordinates().size() + " but was " + biomeRepositoryInMemory.count());
    }

    private void saveTiles(Long id, World persistentWorld) {
        World newWorld = worldRepositoryInMemory.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(persistentWorld);
        List<Tile> tiles = persistentWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(tile -> tile.createBasicClone(newWorld.getCoordinate(tile.getCoordinate().getXCoord(), tile.getCoordinate().getYCoord())))
                .collect(Collectors.toList());
        tiles.forEach(tile -> tile.getCoordinate().setTile(tile));
        log.info("Current tiles in memory: " + tiles.size() + " " + Arrays.toString(tiles.stream().map(Tile::getId).toArray()));
        tileRepositoryInMemory.saveAll(tiles);
        worldRepositoryInMemory.save(newWorld);
        Assert.isTrue(persistentWorld.getCoordinates().size() == tileRepositoryInMemory.count(), "Expected " + persistentWorld.getCoordinates().size() + " but was " + tileRepositoryInMemory.count());
    }

    private void saveCoordinates(Long id, World persistentWorld) {
        World newWorld = worldRepositoryInMemory.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(persistentWorld);
        List<Coordinate> coordinates = persistentWorld.getCoordinates().stream()
                .map(coordinate -> coordinate.createBasicClone(newWorld))
                .collect(Collectors.toList());
        newWorld.getCoordinates().addAll(coordinates);
        log.info("Current coordinates in memory: " + coordinates.size() + " " + Arrays.toString(coordinates.stream().map(Coordinate::getId).toArray()));
        coordinateRepositoryInMemory.saveAll(coordinates);
        worldRepositoryInMemory.save(newWorld);
        Assert.isTrue(persistentWorld.getCoordinates().size() == coordinateRepositoryInMemory.count(),"Expected " + persistentWorld.getCoordinates().size() + " but was " + coordinateRepositoryInMemory.count());
    }

    private void saveContinents(Long id, World persistentWorld) {
        World newWorld = worldRepositoryInMemory.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(persistentWorld);
        List<Continent> continents = persistentWorld.getContinents().stream()
                .map(continent -> continent.createClone(newWorld))
                .collect(Collectors.toList());
        newWorld.getContinents().addAll(continents);
        log.info("Current continents in memory: " + continents.size() + " " + Arrays.toString(continents.stream().map(Continent::getId).toArray()));
        continentRepositoryInMemory.saveAll(continents);
        worldRepositoryInMemory.save(newWorld);
        Assert.isTrue(persistentWorld.getContinents().size() == continentRepositoryInMemory.count(), "Expected " + persistentWorld.getContinents().size() + " but was " + continentRepositoryInMemory.count());
    }

    private void saveBasicWorld(World memoryWorld, World persistentWorld) {
        memoryWorld.createBasicClone(persistentWorld);
        controlWorld(memoryWorld);
        worldRepositoryInMemory.save(memoryWorld);
    }

    private void controlWorld(World world) {
        Assert.notNull(world, "world is null");
        world.fillTransactionals();
        Assert.isTrue(world.getContinents().stream().noneMatch(continent -> continent.getId() == null), "a continent is nukk");
        Assert.isTrue(world.getActorList().stream().noneMatch(actor -> actor.getId() == null), "a actor was null");
        Assert.isTrue(world.getCoordinates().stream().noneMatch(coordinate -> coordinate.getTile().getBiome().getId() == null || coordinate.getTile().getBiome().getTile() == null), "a biome or tile was null");
    }
}
