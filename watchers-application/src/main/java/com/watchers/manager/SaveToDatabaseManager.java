package com.watchers.manager;

import com.watchers.model.actors.Actor;
import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SaveToDatabaseManager {

    @PersistenceContext
    private final EntityManager entityManager;

    public void complexSaveToMemory(World persistentWorld) {
        adjustAndMergeContinents(persistentWorld);
        adjustAndMergeActors(persistentWorld);

        World newWorld = saveBasicWorld(persistentWorld);
        saveContinents(persistentWorld, newWorld);
        saveCoordinates(persistentWorld, newWorld);
        saveActors(persistentWorld, newWorld);
        log.info("World " + persistentWorld.getId() + " is loaded from persistence into memory.");
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

        if (continentMapping.containsKey(persistentWorld.getLastContinentInFlux())) {
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

    private void saveActors(World persistentWorld, World newWorld) {
        log.info("Loading actors.");
        //Assert.isTrue(0 == actorRepository.count(), "Expected 0 but was " + actorRepository.count());

        List<Actor> actors = persistentWorld.getCoordinates().stream()
                .peek(coordinate -> coordinate.getActors().forEach(actor -> actor.setCoordinate(coordinate)))
                .map(Coordinate::getActors)
                .flatMap(Collection::stream)
                .map(actor -> actor.createClone(newWorld.getCoordinate(actor.getCoordinate().getXCoord(), actor.getCoordinate().getYCoord())))
                .sorted(Comparator.comparing(Actor::getId))
                .collect(Collectors.toList());
        actors.forEach(actor -> actor.getCoordinate().getActors().add(actor));
        log.debug(actors.size() + " " + Arrays.toString(actors.stream().map(Actor::getId).toArray()));

        //actorRepository.saveAll(actors);
        saveActorsMethod(actors);
        log.info("Current actors in memory: " + actors.size());
        //Assert.isTrue(persistentWorld.getActorList().size() == actorRepository.count(), "Expected " + persistentWorld.getActorList().size() + " but was " + actorRepository.count());
        log.info("Actors loaded.");
    }

    private void saveCoordinates(World persistentWorld, World newWorld) {
        log.info("Loading coordinates.");
        //World newWorld = worldRepository.findById(id).orElseThrow(() -> new AssertException("world not found"));
        adjustAndMergeAircurrents(persistentWorld);

        List<Coordinate> coordinates = persistentWorld.getCoordinates().parallelStream()
                .map(coordinate -> coordinate.createBasicClone(newWorld))
                .sorted(Comparator.comparing(Coordinate::getId))
                .collect(Collectors.toList());

        newWorld.getCoordinates().addAll(coordinates);
        log.info("Adjusting aircurrents for merging");
        adjustAndMergeAircurrents(newWorld);
        log.info("Aircurrents adjusted for merging");

        List<Aircurrent> aircurrents = newWorld.getCoordinates().parallelStream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .flatMap(skyTile -> skyTile.getOutgoingAircurrents().stream())
                .collect(Collectors.toList());

        newWorld.getCoordinates().parallelStream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .forEach(skyTile -> {
                    skyTile.getIncommingAircurrents().clear();
                    skyTile.getOutgoingAircurrents().clear();
                });

        log.debug("Current coordinates in memory: " + coordinates.size() + " " + Arrays.toString(coordinates.stream().map(Coordinate::getId).toArray()) + ".");

        saveOCoordinateMethod(coordinates);

        //coordinateRepository.saveAll(coordinates);
        log.info("loading coordinates");
        //List<Coordinate> coordinateList = coordinateRepository.findAll();
        log.info("coordinates loaded");

        //Assert.isTrue(persistentWorld.getCoordinates().size() == coordinateList.size(), "Expected " + persistentWorld.getCoordinates().size() + " but was " + coordinateList.size());
        log.info("Coordinates loaded.");

        saveSkies(aircurrents, coordinates);
    }

    private void saveOCoordinateMethod(List<Coordinate> objects) {
        objects.sort(Comparator.comparing(Coordinate::getId));
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for ( int i=0; i<objects.size(); i++ ) {
                Coordinate coordinate = objects.get(i);

                Tile tile = coordinate.getTile();
                Biome biome = tile.getBiome();
                Climate climate = coordinate.getClimate();
                SkyTile skyTile = climate.getSkyTile();

                coordinate.getTile().setBiome(null);
                coordinate.setTile(null);
                coordinate.getClimate().setSkyTile(null);
                coordinate.setClimate(null);

                session.save(coordinate);
                session.save(tile);
                session.save(biome);
                session.save(climate);
                session.save(skyTile);

                if ( i % 200 == 0 ) { //1000/5 enitities, same as the JDBC batch size
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                    log.info("processed " + (((double) i)/((double) objects.size())) + " procent");
                }
            }

            log.info("commiting coordinates");

            tx.commit();

            log.info("coordinates commited");
        }
    }

    private void saveContinentsMethod(List<Continent> objects) {
        objects.sort(Comparator.comparing(Continent::getId));
        objects.forEach(continent -> continent.getCoordinates().clear());
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for ( int i=0; i<objects.size(); i++ ) {
                Continent continent = objects.get(i);
                session.save(continent.getDirection());
                session.save(continent);
                if ( i % 500 == 0 ) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }

            log.info("commiting coordinates");

            tx.commit();

            log.info("coordinates commited");
        }
    }

    private void saveActorsMethod(List<Actor> objects) {
        objects.sort(Comparator.comparing(Actor::getId));
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for ( int i=0; i<objects.size(); i++ ) {
                session.save(objects.get(i));
                if ( i % 1000 == 0 ) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }


            tx.commit();

        }
    }

    private void saveAircurrentMethod(List<Aircurrent> aircurrents) {
        aircurrents.sort(Comparator.comparing(Aircurrent::getId));
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for ( int i=0; i<aircurrents.size(); i++ ) {
                session.save(aircurrents.get(i));
                if ( i % 1000 == 0 ) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
        }
    }

    private void saveWorldMethod(World world) {
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            session.save(world);

            tx.commit();
        }
    }

    public SessionFactory getCurrentSessionFromJPA() {
        // JPA and Hibernate SessionFactory example
        EntityManagerFactory emf = entityManager.getEntityManagerFactory();
        EntityManager entityManager = emf.createEntityManager();
        // Get the Hibernate Session from the EntityManager in JPA
        Session session = entityManager.unwrap(org.hibernate.Session.class);
        return session.getSessionFactory();
    }

    @Transactional
    private void saveSkies(List<Aircurrent> aircurrents, List<Coordinate> coordinateList){
        log.info("Loading aircurrents.");
//        Map<Long, SkyTile> skyMap = coordinateList.parallelStream()
//                .map(Coordinate::getClimate)
//                .map(Climate::getSkyTile)
//                .collect(Collectors.toMap(SkyTile::getId, skytile -> skytile));

//        aircurrents.parallelStream().forEach(aircurrent -> {
//            SkyTile endingSky = skyMap.get(aircurrent.getEndingSky().getId());
//            endingSky.getIncommingAircurrents().add(aircurrent);
//            aircurrent.setEndingSky(endingSky);
//
//            SkyTile startingSky = skyMap.get(aircurrent.getStartingSky().getId());
//            startingSky.getOutgoingAircurrents().add(aircurrent);
//            aircurrent.setStartingSky(startingSky);
//        });

        log.info("Aircurrents loaded.");

        saveAircurrentMethod(aircurrents);
    }

    private void saveContinents(World persistentWorld, World newWorld) {
        log.info("Loading continents.");
        List<Continent> continents = persistentWorld.getContinents().stream()
                .map(continent -> continent.createClone(newWorld))
                .sorted(Comparator.comparing(Continent::getId))
                .collect(Collectors.toList());
        newWorld.getContinents().addAll(continents);
        log.debug("Current continents in memory: " + continents.size() + " " + Arrays.toString(continents.stream().map(Continent::getId).toArray()));
        continents.sort(Comparator.comparing(Continent::getId));
        saveContinentsMethod(continents);

        //Assert.isTrue(persistentWorld.getContinents().size() == continentRepository.count(), "Expected " + persistentWorld.getContinents().size() + " but was " + continentRepository.count());
        log.info("Continents loaded.");
    }

    private World saveBasicWorld(World persistentWorld) {
        log.info("Loading the barren world.");
        World memoryWorld = persistentWorld.createBasicClone();

        saveWorldMethod(memoryWorld);

        log.info("Barren world loaded.");
        return memoryWorld;
    }
}

