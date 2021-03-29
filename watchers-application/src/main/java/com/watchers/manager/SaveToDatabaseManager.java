package com.watchers.manager;

import com.mchange.util.AssertException;
import com.watchers.model.actors.Actor;
import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SaveToDatabaseManager {

    private WorldRepository worldRepository;
    private ContinentRepository continentRepository;
    private CoordinateRepository coordinateRepository;
    private ActorRepository actorRepository;

    public void complexSaveToMemory(World persistentWorld) {
        adjustAndMergeContinents(persistentWorld);
        adjustAndMergeActors(persistentWorld);

        Long newWorldId = saveBasicWorld(persistentWorld);
        saveContinents(newWorldId, persistentWorld);
        saveCoordinates(newWorldId, persistentWorld);
        saveActors(newWorldId, persistentWorld);
    }

    /**
     * This method changes the ids of the actors since these need to be ordered from 1 upwards for being able to be
     * processed by Hibernate
     *
     * @param persistentWorld the world that is provided from a persistent source
     */
    private void adjustAndMergeActors(World persistentWorld) {
        List<Actor> actors = persistentWorld.getActorList();
        actors.sort(Comparator.comparing(Actor::getId));
        for (int i = 1; i <= actors.size(); i++) {
            actors.get(i - 1).setId((long) i);
        }
    }

    /**
     * This method is needed so that the continents in the World object are linked with the Continents in the Coordinate object.
     * Also this methode adjusts the Id's and orders them accordingly.
     * Otherwise Hibernate will persist a Continent with a generated Id that is not in accordince with it's givven Id.
     * By inserting them in order of their new Id's, Hibernate will not accidentily merge a Continent with an already
     * persisted Continent.
     *
     * @param persistentWorld the world that is provided from a persistent source
     */
    private void adjustAndMergeContinents(World persistentWorld) {
        Set<Continent> continentSet = persistentWorld.getContinents();
        List<Continent> continents = new ArrayList<>(continentSet);
        continents.sort(Comparator.comparing(Continent::getId));
        Map<Long, Continent> continentMapping = new HashMap<>();
        for (int i = 1; i <= continents.size(); i++) {
            continentMapping.put(continents.get(i - 1).getId(), continents.get(i - 1));
            continents.get(i - 1).setId((long) i);
        }

        persistentWorld.getCoordinates().
                forEach(
                        coordinate -> coordinate.changeContinent(continentMapping.get(coordinate.getContinent().getId()))
                );

        if (continentMapping.keySet().contains(persistentWorld.getLastContinentInFlux())) {
            persistentWorld.setLastContinentInFlux(continentMapping.get(persistentWorld.getLastContinentInFlux()).getId());
        }
    }

    /**
     * Because the world from the json has sepperated the aircurrents to their ending en starting skies,
     * this link needs to be reset. Otherwise the aircurrent link within the skies are not the same entity.
     * This would lead to problems with saving to the inmemory database with hibernate. This method is also used to
     * prepare for creating clones of the skies with the correct aircurrents.
     *
     * @param world world
     */
    private void adjustAndMergeAircurrents(World world) {
        List<SkyTile> skyTiles = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .collect(Collectors.toList());

        skyTiles.forEach(skyTile -> {
            skyTile.getOutgoingAircurrents().forEach(aircurrent -> aircurrent.setStartingSky(skyTile));
            skyTile.getIncommingAircurrents().forEach(aircurrent -> aircurrent.setEndingSky(skyTile));
        });

        List<Aircurrent> incommingAircurrents = skyTiles.stream()
                .flatMap(x-> x.getIncommingAircurrents().stream())
                .collect(Collectors.toList());

        List<Aircurrent> outgoingAircurrents = skyTiles.stream()
                .flatMap(x-> x.getOutgoingAircurrents().stream())
                .collect(Collectors.toList());

        Map<Long, SkyTile> endingSkies = incommingAircurrents.stream()
                .collect(Collectors.toMap(Aircurrent::getId, Aircurrent::getEndingSky, (x,y)-> x));
        Map<Long, SkyTile> startingSkies = outgoingAircurrents.stream()
                .collect(Collectors.toMap(Aircurrent::getId, Aircurrent::getStartingSky, (x,y)-> x));

        skyTiles.forEach(skyTile -> {
            skyTile.getIncommingAircurrents().clear();
            skyTile.getOutgoingAircurrents().clear();
        });

        incommingAircurrents.forEach(aircurrent -> {
            aircurrent.setStartingSky(startingSkies.get(aircurrent.getId()));
            aircurrent.getStartingSky().getOutgoingAircurrents().add(aircurrent);

            aircurrent.setEndingSky(endingSkies.get(aircurrent.getId()));
            aircurrent.getEndingSky().getIncommingAircurrents().add(aircurrent);
        });
    }

    @Transactional
    private void saveActors(Long id, World persistentWorld) {
        Assert.isTrue(0 == actorRepository.count(), "Expected 0 but was " + actorRepository.count());

        World newWorld = worldRepository.findById(id).orElseThrow(() -> new AssertException("world not found"));
        List<Actor> actors = persistentWorld.getCoordinates().stream()
                .peek(coordinate -> coordinate.getActors().forEach(actor -> actor.setCoordinate(coordinate)))
                .map(Coordinate::getActors)
                .flatMap(Collection::stream)
                .map(actor -> actor.createClone(newWorld.getCoordinate(actor.getCoordinate().getXCoord(), actor.getCoordinate().getYCoord())))
                .sorted(Comparator.comparing(Actor::getId))
                .collect(Collectors.toList());
        actors.forEach(actor -> actor.getCoordinate().getActors().add(actor));
        log.info(actors.size() + " " + Arrays.toString(actors.stream().map(Actor::getId).toArray()));
        actorRepository.saveAll(actors);
        log.info("Current actors in memory: " + actors.size());
        Assert.isTrue(persistentWorld.getActorList().size() == actorRepository.count(), "Expected " + persistentWorld.getActorList().size() + " but was " + actorRepository.count());
    }

    @Transactional
    private void saveCoordinates(Long id, World persistentWorld) {
        World newWorld = worldRepository.findById(id).orElseThrow(() -> new AssertException("world not found"));
        adjustAndMergeAircurrents(persistentWorld);

        List<Coordinate> coordinates = persistentWorld.getCoordinates().stream()
                .map(coordinate -> coordinate.createBasicClone(newWorld))
                .sorted(Comparator.comparing(Coordinate::getId))
                .collect(Collectors.toList());

        newWorld.getCoordinates().addAll(coordinates);
        adjustAndMergeAircurrents(newWorld);

        List<Aircurrent> aircurrents = newWorld.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .flatMap(skyTile -> skyTile.getOutgoingAircurrents().stream())
                .collect(Collectors.toList());

        newWorld.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .forEach(skyTile -> {
                    skyTile.getIncommingAircurrents().clear();
                    skyTile.getOutgoingAircurrents().clear();
                });

        log.info("Current coordinates in memory: " + coordinates.size() + " " + Arrays.toString(coordinates.stream().map(Coordinate::getId).toArray()));
        coordinateRepository.saveAll(coordinates);


        List<Coordinate> coordinateList = coordinateRepository.findAll();

        Map<Long, SkyTile> skyMap = coordinateList.stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .collect(Collectors.toMap(SkyTile::getId, skytile -> skytile));

        aircurrents.forEach(aircurrent -> {
            SkyTile endingSky = skyMap.get(aircurrent.getEndingSky().getId());
            endingSky.getIncommingAircurrents().add(aircurrent);
            aircurrent.setEndingSky(endingSky);

            SkyTile startingSky = skyMap.get(aircurrent.getStartingSky().getId());
            startingSky.getOutgoingAircurrents().add(aircurrent);
            aircurrent.setStartingSky(startingSky);
        });

        coordinateRepository.saveAll(coordinateList);

        Assert.isTrue(persistentWorld.getCoordinates().size() == coordinateRepository.count(), "Expected " + persistentWorld.getCoordinates().size() + " but was " + coordinateRepository.count());
    }

    @Transactional
    private void saveContinents(Long id, World persistentWorld) {
        World newWorld = worldRepository.findById(id).orElseThrow(() -> new AssertException("world not found"));
        List<Continent> continents = persistentWorld.getContinents().stream()
                .map(continent -> continent.createClone(newWorld))
                .sorted(Comparator.comparing(Continent::getId))
                .collect(Collectors.toList());
        newWorld.getContinents().addAll(continents);
        log.info("Current continents in memory: " + continents.size() + " " + Arrays.toString(continents.stream().map(Continent::getId).toArray()));
        continents.sort(Comparator.comparing(Continent::getId));
        continentRepository.saveAll(continents);

        Assert.isTrue(persistentWorld.getContinents().size() == continentRepository.count(), "Expected " + persistentWorld.getContinents().size() + " but was " + continentRepository.count());
    }

    @Transactional
    private Long saveBasicWorld(World persistentWorld) {
        World memoryWorld = persistentWorld.createBasicClone();
        World flushedWorld = worldRepository.saveAndFlush(memoryWorld);
        return flushedWorld.getId();
    }
}

