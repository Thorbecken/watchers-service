package com.watchers.service;

import com.mchange.util.AssertException;
import com.watchers.model.actor.Actor;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.repository.postgres.*;
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
public class PersistenceSaveService {

    private WorldRepositoryPersistent worldRepositoryPersistent;
    private ContinentRepositoryPersistent continentRepositoryPersistent;
    private CoordinateRepositoryPersistent coordinateRepositoryPersistent;
    private TileRepositoryPersistent tileRepositoryPersistent;
    private BiomeRepositoryPersistent biomeRepositoryPersistent;
    private ActorRepositoryPersistent actorRepositoryPersistent;

    public void complexSaveToPersistence(World memoryWorld) {
        World persistentWorld = new World(memoryWorld.getXSize(), memoryWorld.getYSize());
        persistentWorld.setId(memoryWorld.getId());
        worldRepositoryPersistent.save(persistentWorld);

        saveBasicWorld(persistentWorld, memoryWorld);
        saveContinents(persistentWorld.getId(), memoryWorld);
        saveCoordinates(persistentWorld.getId(), memoryWorld);
        saveTiles(persistentWorld.getId(), memoryWorld);
        saveBiomes(persistentWorld.getId(), memoryWorld);
        saveActors(persistentWorld.getId(), memoryWorld);
    }

    private void saveActors(Long id, World memoryWorld) {
        Assert.isTrue(0 == actorRepositoryPersistent.count(), "Expected 0 but was " + actorRepositoryPersistent.count());

        World persistenWorld = worldRepositoryPersistent.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(memoryWorld);
        List<Actor> actors = memoryWorld.getActorList().stream()
                .map(actor -> actor.createClone(persistenWorld.getCoordinate(actor.getCoordinate().getXCoord(), actor.getCoordinate().getYCoord())))
                .collect(Collectors.toList());
        actors.forEach(actor -> actor.getCoordinate().getActors().add(actor));
        log.info(actors.size() + " " + Arrays.toString(actors.stream().map(Actor::getId).toArray()));
        actorRepositoryPersistent.saveAll(actors);
        //worldRepositoryPersistent.save(persistenWorld);
        Iterable<Actor> actorz =  actorRepositoryPersistent.findAll();
        log.info("Current actors in persistence: " + StreamSupport.stream(actorz.spliterator(),false)
                .map(Actor::getCoordinate)
                .map(Coordinate::getId)
                .collect(Collectors.toList()).toString());
        Assert.isTrue(memoryWorld.getActorList().size() == actorRepositoryPersistent.count(), "Expected " + memoryWorld.getActorList().size() + " but was " + actorRepositoryPersistent.count());
    }

    private void saveBiomes(Long id, World memeoryWorld) {
        World persistenWorld = worldRepositoryPersistent.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(memeoryWorld);
        List<Biome> biomes = memeoryWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getBiome)
                .map(biome -> biome.createClone(persistenWorld.getCoordinate(biome.getTile().getCoordinate().getXCoord(), biome.getTile().getCoordinate().getYCoord()).getTile()))
                .collect(Collectors.toList());
        biomes.forEach(biome -> biome.getTile().setBiome(biome));
        log.info("Current biomes in persistence: " + biomes.size() + " " + Arrays.toString(biomes.stream().map(Biome::getId).toArray()));
        biomeRepositoryPersistent.saveAll(biomes);
        //worldRepositoryPersistent.save(persistenWorld);
        Assert.isTrue(memeoryWorld.getCoordinates().size() == biomeRepositoryPersistent.count(), "Expected " + memeoryWorld.getCoordinates().size() + " but was " + biomeRepositoryPersistent.count());
    }

    private void saveTiles(Long id, World memoryWorld) {
        World persistentWorld = worldRepositoryPersistent.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(memoryWorld);
        List<Tile> tiles = memoryWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(tile -> tile.createBasicClone(persistentWorld.getCoordinate(tile.getCoordinate().getXCoord(), tile.getCoordinate().getYCoord())))
                .collect(Collectors.toList());
        tiles.forEach(tile -> tile.getCoordinate().setTile(tile));
        log.info("Current tiles in persistence: " + tiles.size() + " " + Arrays.toString(tiles.stream().map(Tile::getId).toArray()));
        tileRepositoryPersistent.saveAll(tiles);
        //worldRepositoryPersistent.save(persistentWorld);
        Assert.isTrue(memoryWorld.getCoordinates().size() == tileRepositoryPersistent.count(), "Expected " + memoryWorld.getCoordinates().size() + " but was " + tileRepositoryPersistent.count());
    }

    private void saveCoordinates(Long id, World memoryWorld) {
        World persistentWorld = worldRepositoryPersistent.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(memoryWorld);
        List<Coordinate> coordinates = memoryWorld.getCoordinates().stream()
                .map(coordinate -> coordinate.createBasicClone(persistentWorld))
                .collect(Collectors.toList());
        persistentWorld.getCoordinates().addAll(coordinates);
        log.info("Current coordinates in persistence: " + coordinates.size() + " " + Arrays.toString(coordinates.stream().map(Coordinate::getId).toArray()));
        coordinateRepositoryPersistent.saveAll(coordinates);
        //worldRepositoryPersistent.save(persistentWorld);
        Assert.isTrue(memoryWorld.getCoordinates().size() == coordinateRepositoryPersistent.count(),"Expected " + memoryWorld.getCoordinates().size() + " but was " + coordinateRepositoryPersistent.count());
    }

    private void saveContinents(Long id, World memoryWorld) {
        World perstistentWorld = worldRepositoryPersistent.findById(id).orElseThrow(() -> new AssertException("world not found"));
        controlWorld(memoryWorld);
        List<Continent> continents = memoryWorld.getContinents().stream()
                .map(continent -> continent.createClone(perstistentWorld))
                .collect(Collectors.toList());
        perstistentWorld.getContinents().addAll(continents);
        log.info("Current continents in persistence: " + continents.size() + " " + Arrays.toString(continents.stream().map(Continent::getId).toArray()));
        continentRepositoryPersistent.saveAll(continents);
        //worldRepositoryPersistent.save(perstistentWorld);
        Assert.isTrue(memoryWorld.getContinents().size() == continentRepositoryPersistent.count(), "Expected " + memoryWorld.getContinents().size() + " but was " + continentRepositoryPersistent.count());
    }

    private void saveBasicWorld(World persistentWorld, World memoryWorld) {
        persistentWorld.createBasicClone(memoryWorld);
        controlWorld(persistentWorld);
        worldRepositoryPersistent.save(persistentWorld);
    }

    private void controlWorld(World world) {
        Assert.notNull(world, "world is null");
        world.fillTransactionals();
        Assert.isTrue(world.getContinents().stream().noneMatch(continent -> continent.getId() == null), "a continent is nukk");
        Assert.isTrue(world.getActorList().stream().noneMatch(actor -> actor.getId() == null), "a actor was null");
        Assert.isTrue(world.getCoordinates().stream().noneMatch(coordinate -> coordinate.getTile().getBiome().getId() == null || coordinate.getTile().getBiome().getTile() == null), "a biome or tile was null");
    }
}
