package com.watchers.manager;

import com.watchers.model.actors.Actor;
import com.watchers.model.climate.*;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.River;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.Watershed;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.model.world.WorldSettings;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

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
        complexSaveToMemory(persistentWorld, false);
    }

    public void complexSaveToMemory(World persistentWorld, boolean freshlyCreated) {
        if (freshlyCreated) {
            assignId(persistentWorld);
        }
        adjustAndMergeContinents(persistentWorld);
        adjustAndMergeWatersheds(persistentWorld);
        adjustAndMergeActors(persistentWorld);

        World newWorld = saveBasicWorld(persistentWorld);
        saveContinents(persistentWorld, newWorld);
        saveWatersheds(persistentWorld, newWorld);
        saveCoordinates(persistentWorld, newWorld, freshlyCreated);
        log.info("World " + persistentWorld.getId() + " is loaded from persistence into memory.");
    }

    private void assignId(World persistentWorld) {
        if (persistentWorld.getId() == null) {
            persistentWorld.setId(1L);
            persistentWorld.setLastContinentInFlux(1);
        }
        List<Continent> continents = persistentWorld.getContinents().stream()
                .filter(continent -> continent.getId() == null)
                .collect(Collectors.toList());

        List<Watershed> watersheds = persistentWorld.getWatersheds().stream()
                .filter(continent -> continent.getId() == null)
                .collect(Collectors.toList());

        List<Coordinate> coordinates = persistentWorld.getCoordinates().stream()
                .filter(coordinate -> coordinate.getId() == null)
                .collect(Collectors.toList());

        List<Actor> actors = persistentWorld.getActorList().stream()
                .filter(actor -> actor.getId() == null)
                .collect(Collectors.toList());

        List<River> rivers = persistentWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getRiver)
                .filter(Objects::nonNull)
                .filter(river -> river.getId() == null)
                .collect(Collectors.toList());

        List<Aircurrent> aircurrents = persistentWorld.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .map(SkyTile::getIncommingAircurrents)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (long i = 0; i < continents.size(); i++) {
            continents.get((int) i).setId(i + 1);
        }
        for (long i = 0; i < watersheds.size(); i++) {
            watersheds.get((int) i).setId(i + 1);
        }
        for (long i = 0; i < actors.size(); i++) {
            actors.get((int) i).setId(i + 1);
        }
        for (long i = 0; i < rivers.size(); i++) {
            rivers.get((int) i).setId(i + 1);
        }
        for (long i = 0; i < aircurrents.size(); i++) {
            aircurrents.get((int) i).setId(i + 1);
        }
        for (long i = 0; i < coordinates.size(); i++) {
            setCoordinateAndNestedClassesIds(coordinates.get((int) i), i + 1);
        }
    }

    private void setCoordinateAndNestedClassesIds(Coordinate coordinate, Long id) {
        coordinate.setId(id);
        coordinate.getTile().setId(id);
        coordinate.getTile().getBiome().setId(id);

        coordinate.getClimate().setId(id);
        coordinate.getClimate().getSkyTile().setId(id);
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
     * Otherwise Hibernate will persist a Continent with a generated Id that is not in accordince with its given Id.
     * By inserting them in order of their new Id's, Hibernate will not accidentily merge a Continent with an already
     * persisted Continent.
     *
     * @param persistentWorld the world that is provided from a persistent source
     */
    private void adjustAndMergeContinents(World persistentWorld) {
        Set<Continent> continentSet = persistentWorld.getContinents();
        List<Continent> continents = new ArrayList<>(continentSet);
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
     * This method is needed so that the watersheds in the World object are linked with the watersheds in the Coordinate object.
     * Also this methode adjusts the Id's and orders them accordingly.
     * Otherwise Hibernate will persist a Continent with a generated Id that is not in accordince with its given Id.
     * By inserting them in order of their new Id's, Hibernate will not accidentily merge a Watershed with an already
     * persisted Continent.
     *
     * @param persistentWorld the world that is provided from a persistent source
     */
    private void adjustAndMergeWatersheds(World persistentWorld) {
        Set<Watershed> watershedSet = persistentWorld.getWatersheds();
        List<Watershed> watersheds = new ArrayList<>(watershedSet);
        Map<Long, Watershed> watershedHashMap = new HashMap<>();
        for (int i = 1; i <= watersheds.size(); i++) {
            watershedHashMap.put(watersheds.get(i - 1).getId(), watersheds.get(i - 1));
            watersheds.get(i - 1).setId((long) i);
        }

        persistentWorld.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getWatershed() != null)
                .forEach(
                        tile -> tile.changeWatershed(watershedHashMap.get(tile.getWatershed().getId()))
                );
    }

    /**
     * Because the world from the json has sepperated the aircurrents to their ending en starting skies,
     * this link needs to be reset. Otherwise the aircurrent link within the skies are not the same entity.
     * This would lead to problems with saving to the inmemory database with hibernate. This method is also used to
     * prepare for creating clones of the skies with the correct aircurrents.
     *
     * @param world world
     */
    private void adjustAndMergeAircurrents(World world, boolean newWorld, boolean freshlyCreated) {
        List<SkyTile> skyTiles = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .map(Climate::getSkyTile)
                .collect(Collectors.toList());

        if (!newWorld) {
            if (freshlyCreated) {
                Map<Long, List<Aircurrent>> outgoingAircurrentsOfStartingskiesFromIncommingAircurrents = skyTiles.stream()
                        .flatMap(skyTile -> skyTile.getIncommingAircurrents().stream())
                        .collect(Collectors.toMap(incommingAircurrent -> incommingAircurrent.getStartingSky().getId()
                                , Collections::singletonList
                                , (x, y) -> {
                                    List<Aircurrent> aircurrents = new ArrayList<>(x);
                                    aircurrents.addAll(y);
                                    return aircurrents;
                                }));

                skyTiles.forEach(skyTile -> {
                    skyTile.getOutgoingAircurrents().clear();
                    skyTile.getOutgoingAircurrents().addAll(outgoingAircurrentsOfStartingskiesFromIncommingAircurrents.get(skyTile.getId()));
                    skyTile.getOutgoingAircurrents().forEach(aircurrent -> aircurrent.setStartingSky(skyTile));
                });

                return;
            } else {
                skyTiles.forEach(skyTile -> {
                    skyTile.getOutgoingAircurrents().forEach(aircurrent -> aircurrent.setStartingSky(skyTile));
                    skyTile.getIncommingAircurrents().forEach(aircurrent -> aircurrent.setEndingSky(skyTile));
                });

                Map<Long, SkyTile> endingSkyIdForAircurrents = skyTiles.stream()
                        .flatMap(skyTile -> skyTile.getIncommingAircurrents().stream())
                        .collect(Collectors.toMap(Aircurrent::getId, Aircurrent::getEndingSky));
                skyTiles.stream()
                        .map(SkyTile::getOutgoingAircurrents)
                        .flatMap(Collection::stream)
                        .forEach(outgoingAircurrent -> outgoingAircurrent.setStartingSky(
                                endingSkyIdForAircurrents.get(outgoingAircurrent.getStartingSky().getId())));
                return;
            }
        }

        List<Aircurrent> incommingAircurrents = skyTiles.stream()
                .map(SkyTile::getIncommingAircurrents)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Map<Long, Aircurrent> outgoingAircurrentIdMap = skyTiles.stream()
                .map(SkyTile::getOutgoingAircurrents)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Aircurrent::getId, aircurrent -> aircurrent));
        skyTiles.forEach(skyTile -> skyTile.getOutgoingAircurrents().clear());
        incommingAircurrents.forEach(incommingAircurrent -> {
            Aircurrent outgoingAircurrent = outgoingAircurrentIdMap.get(incommingAircurrent.getId());
            SkyTile startingSky = outgoingAircurrent.getStartingSky();
            incommingAircurrent.setStartingSky(startingSky);
            startingSky.getOutgoingAircurrents().add(incommingAircurrent);
        });

        Map<Long, SkyTile> skyTileMap = skyTiles.stream()
                .collect(Collectors.toMap(SkyTile::getId, x -> x));

        skyTiles.forEach(skyTile -> {
            skyTile.getOutgoingAircurrents().forEach(aircurrent -> {
                aircurrent.setEndingSky(skyTileMap.get(aircurrent.getEndingSky().getId()));
                aircurrent.setStartingSky(skyTileMap.get(aircurrent.getStartingSky().getId()));
            });
            skyTile.getIncommingAircurrents().forEach(aircurrent -> {
                aircurrent.setEndingSky(skyTileMap.get(aircurrent.getEndingSky().getId()));
                aircurrent.setStartingSky(skyTileMap.get(aircurrent.getStartingSky().getId()));
            });
        });
    }

    private void saveCoordinates(World persistentWorld, World newWorld, boolean freshlyCreated) {
        log.info("Loading coordinates.");
        if (freshlyCreated) {
            adjustAndMergeAircurrents(persistentWorld, false, freshlyCreated);
        } else {
            adjustAndMergeAircurrents(persistentWorld, false, freshlyCreated);
        }

        List<Coordinate> coordinates = persistentWorld.getCoordinates().stream()
                .map(coordinate -> coordinate.createClone(newWorld))
                .sorted(Comparator.comparing(Coordinate::getId))
                .collect(Collectors.toList());

        newWorld.getCoordinates().addAll(coordinates);
        log.info("Adjusting aircurrents for merging");
        adjustAndMergeAircurrents(newWorld, true, freshlyCreated);
        log.info("Aircurrents adjusted for merging");

        log.debug("Current coordinates in memory: " + coordinates.size() + " " + Arrays.toString(coordinates.stream().map(Coordinate::getId).toArray()) + ".");

        saveCoordinateMethod(coordinates);
    }

    @Data
    private static class CoordinateHolder {
        private final Long id;
        private final Coordinate coordinate;
        private final Tile tile;
        private final Climate climate;
        private final Set<Actor> actors;

        CoordinateHolder(Coordinate coordinate) {
            this.id = coordinate.getId();
            this.coordinate = coordinate;
            this.tile = coordinate.getTile();
            this.climate = coordinate.getClimate();
            this.actors = coordinate.getActors();
        }

        private void clearInformation() {
            coordinate.setId(null);
            coordinate.setTile(null);
            coordinate.setClimate(null);
            coordinate.setActors(null);
        }

        private void setInformation() {
            coordinate.setActors(this.actors);
        }
    }

    private void saveCoordinateMethod(List<Coordinate> coordinates) {
        coordinates.sort(Comparator.comparing(Coordinate::getId));

        List<CoordinateHolder> coordinateHolderList = coordinates.stream()
                .map(CoordinateHolder::new)
                .collect(Collectors.toList());

        coordinateHolderList.forEach(CoordinateHolder::clearInformation);

        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < coordinates.size(); i++) {
                Coordinate coordinate = coordinates.get(i);
                session.save(coordinate);

                if (i == 1000) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();

                    log.info("processed " + (((double) i) / ((double) coordinates.size())) + " procent of coordinates.");
                }
            }

            session.flush();
            session.clear();

            log.info("commiting coordinates and sub classes.");

            tx.commit();

            log.info("coordinates and sub classes commited.");
        }

        List<Climate> climates = coordinateHolderList.stream()
                .map(CoordinateHolder::getClimate)
                .collect(Collectors.toList());
        saveClimates(climates);

        List<Tile> tiles = coordinateHolderList.stream()
                .map(CoordinateHolder::getTile)
                .collect(Collectors.toList());
        saveTiles(tiles);

        coordinateHolderList.forEach(CoordinateHolder::setInformation);
        Set<Actor> actors = coordinateHolderList.stream()
                .flatMap(coordinateHolder -> coordinateHolder.getActors().stream())
                .collect(Collectors.toSet());
        saveActors(new ArrayList<>(actors));
    }

    private void saveActors(List<Actor> actors) {
        actors.forEach(actor -> actor.setId(null));

        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < actors.size(); i++) {
                Actor actor = actors.get(i);
                session.save(actor);

                if (i == 1000) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();

                    log.info("processed " + (((double) i) / ((double) actors.size())) + " procent of actors.");
                }
            }

            session.flush();
            session.clear();
            tx.commit();
        }

    }
    @Data
    private static class WatershedHolder {
        private final Watershed watershed;
        private List<River> riverFlow;
        private List<Tile> watershedTiles;

        WatershedHolder (Watershed watershed){
            this.watershed = watershed;
            if(watershed != null) {
                riverFlow = watershed.getRiverFlow();
                watershedTiles = watershed.getWatershedTiles();
            }
        }

        void clearInformation() {
            if(watershed != null) {
                watershed.clearInformation();
            }
        }

        void setInformation(){
            if(watershed != null) {
                watershed.setInformation(riverFlow, watershedTiles);
            }
        }
    }
    @Data
    private static class TileHolder {
        private final Tile tile;
        private final Biome biome;
        private final River river;
        private final WatershedHolder watershedHolder;

        TileHolder(Tile tile) {
            this.tile = tile;
            this.biome = tile.getBiome();
            this.river = tile.getRiver();
            this.watershedHolder = new WatershedHolder(tile.getWatershed());
        }

        void clearInformation() {
            tile.setId(null);
            tile.setBiome(null);
            tile.setRiver(null);
            watershedHolder.clearInformation();
        }

        void setInformation(){
            watershedHolder.setInformation();
        }
    }

    private void saveTiles(List<Tile> tiles) {
        tiles.forEach(tile -> tile.getCoordinate().setTile(tile));
        List<TileHolder> tileHolderList = tiles.stream()
                .map(TileHolder::new)
                .collect(Collectors.toList());
        tileHolderList.forEach(TileHolder::clearInformation);

        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < tiles.size(); i++) {
                Tile tile = tiles.get(i);
                session.save(tile);

                if (i == 1000) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();

                    log.info("processed " + (((double) i) / ((double) tiles.size())) + " procent of tiles.");
                }
            }

            List<Biome> biomes = tileHolderList.stream()
                    .map(TileHolder::getBiome)
                    .collect(Collectors.toList());

            session.flush();
            session.clear();
            tx.commit();

            saveBiomes(biomes);

            List<River> rivers = tileHolderList.stream()
                    .map(TileHolder::getRiver)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            saveRivers(rivers);

            tileHolderList.forEach(TileHolder::setInformation);
        }
    }

    private void saveBiomes(List<Biome> biomes) {
        biomes.forEach(biome -> biome.getTile().setBiome(biome));
        biomes.forEach(biome -> biome.setId(null));

        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < biomes.size(); i++) {
                Biome biome = biomes.get(i);
                session.save(biome);

                if (i == 1000) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();

                    log.info("processed " + (((double) i) / ((double) biomes.size())) + " procent of biomes.");
                }
            }

            session.flush();
            session.clear();
            tx.commit();
        }
    }

    private void saveRivers(List<River> rivers) {
        rivers.forEach(river -> river.getTile().setRiver(river));
        rivers.forEach(river -> river.setId(null));

        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < rivers.size(); i++) {
                River river = rivers.get(i);
                session.save(river);

                if (i == 1000) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();

                    log.info("processed " + (((double) i) / ((double) rivers.size())) + " procent of rivers.");
                }
            }

            session.flush();
            session.clear();
            tx.commit();
        }
    }

    @Data
    private static class ClimateHolder {
        private final Climate climate;
        private final SkyTile skyTile;

        ClimateHolder(Climate climate) {
            this.climate = climate;
            this.skyTile = climate.getSkyTile();
        }

        void clearInformation() {
            climate.setId(null);
            climate.setSkyTile(null);
        }
    }

    private void saveClimates(List<Climate> climates) {
        climates.forEach(climate -> climate.getCoordinate().setClimate(climate));
        List<ClimateHolder> climateHolderList = climates.stream()
                .map(ClimateHolder::new)
                .collect(Collectors.toList());
        climateHolderList.forEach(ClimateHolder::clearInformation);

        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            for (int i = 0; i < climates.size(); i++) {
                Climate climate = climates.get(i);
                session.save(climate);

                if (i == 1000) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();

                    log.info("processed " + (((double) i) / ((double) climates.size())) + " procent of climates.");
                }
            }

            session.flush();
            session.clear();
            tx.commit();
        }

        List<SkyTile> skyTiles = climateHolderList.stream()
                .map(ClimateHolder::getSkyTile)
                .collect(Collectors.toList());
        saveSkyTiles(skyTiles);
    }

    @Data
    private static class SkyTileHolder {
        private final SkyTile skyTile;
        private final Set<Aircurrent> incommingAircurrent;
        private final Set<Aircurrent> outgoingAircurrent;

        SkyTileHolder(SkyTile skyTile) {
            this.skyTile = skyTile;
            this.incommingAircurrent = skyTile.getIncommingAircurrents();
            this.outgoingAircurrent = skyTile.getOutgoingAircurrents();
        }

        void clearInformation() {
            skyTile.setId(null);
            skyTile.setOutgoingAircurrents(null);
            skyTile.setIncommingAircurrents(null);
        }

        void setInformation() {
            skyTile.setOutgoingAircurrents(outgoingAircurrent);
            skyTile.setIncommingAircurrents(incommingAircurrent);
        }
    }

    private void saveSkyTiles(List<SkyTile> skyTiles) {
        skyTiles.forEach(skyTile -> skyTile.getClimate().setSkyTile(skyTile));
        List<SkyTileHolder> skyTileHolderList = skyTiles.stream()
                .map(SkyTileHolder::new)
                .collect(Collectors.toList());
        skyTileHolderList.forEach(SkyTileHolder::clearInformation);

        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < skyTiles.size(); i++) {
                SkyTile skyTile = skyTiles.get(i);
                session.save(skyTile);

                if (i == 1000) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();

                    log.info("processed " + (((double) i) / ((double) skyTiles.size())) + " procent of skyTiles.");
                }
            }

            session.flush();
            session.clear();
            tx.commit();
        }

        skyTileHolderList.forEach(SkyTileHolder::setInformation);

        List<Aircurrent> aircurrents = skyTileHolderList.stream()
                .map(SkyTileHolder::getIncommingAircurrent)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        aircurrents.forEach(Aircurrent::resetCoordinates);
        saveAircurrentMethod(aircurrents);
    }

    private void saveContinentsMethod(List<Continent> objects) {
        objects.sort(Comparator.comparing(Continent::getId));
        objects.forEach(continent -> continent.getCoordinates().clear());
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < objects.size(); i++) {
                Continent continent = objects.get(i);
                session.save(continent.getDirection());
                session.save(continent);
                if (i % 500 == 0) {
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

    private void saveWatershedsMethod(List<Watershed> objects) {
        objects.sort(Comparator.comparing(Watershed::getId));
        objects.forEach(watershed -> watershed.getWatershedTiles().clear());
        objects.forEach(watershed -> watershed.getRiverFlow().clear());
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < objects.size(); i++) {
                Watershed watershed = objects.get(i);
                session.save(watershed);
                if (i % 500 == 0) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }

            log.info("commiting watersheds");

            tx.commit();

            log.info("watersheds commited");
        }
    }

    private void saveAircurrentMethod(List<Aircurrent> aircurrents) {
        aircurrents.sort(Comparator.comparing(Aircurrent::getId));
        aircurrents.forEach(aircurrent -> aircurrent.setId(null));
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            for (int i = 0; i < aircurrents.size(); i++) {
                session.save(aircurrents.get(i));
                if (i % 1000 == 0) {
                    //flush a batch of inserts and release memory:
                    session.flush();
                    session.clear();
                }
            }

            session.flush();
            session.clear();
            tx.commit();
        }
    }

    private void saveWorldMethod(World world) {
        SessionFactory sessionFactory = getCurrentSessionFromJPA();
        WorldMetaData worldMetaData = world.getWorldMetaData();
        WorldSettings worldSettings = world.getWorldSettings();

        world.setWorldMetaData(null);
        world.setWorldSettings(null);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            session.save(world);

            tx.commit();
        }

        world.setWorldMetaData(worldMetaData);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            session.save(worldMetaData);

            tx.commit();

        }

        world.setWorldSettings(worldSettings);

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            session.save(worldSettings);

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

    private void saveContinents(World persistentWorld, World newWorld) {
        log.info("Loading continents.");
        List<Continent> continents = persistentWorld.getContinents().stream()
                .map(continent -> continent.createClone(newWorld))
                .collect(Collectors.toList());
        newWorld.getContinents().addAll(continents);
        log.debug("Current continents in memory: " + continents.size() + " " + Arrays.toString(continents.stream().map(Continent::getId).toArray()));
        continents.sort(Comparator.comparing(Continent::getId));
        saveContinentsMethod(continents);

        //Assert.isTrue(persistentWorld.getContinents().size() == continentRepository.count(), "Expected " + persistentWorld.getContinents().size() + " but was " + continentRepository.count());
        log.info("Continents loaded.");
    }

    private void saveWatersheds(World persistentWorld, World newWorld) {
        log.info("Loading watersheds.");
        List<Watershed> watersheds = persistentWorld.getWatersheds().stream()
                .map(watershed -> watershed.createClone(newWorld))
                .collect(Collectors.toList());
        newWorld.getWatersheds().addAll(watersheds);
        log.debug("Current watersheds in memory: " + watersheds.size() + " " + Arrays.toString(watersheds.stream().map(Watershed::getId).toArray()));
        watersheds.sort(Comparator.comparing(Watershed::getId));
        saveWatershedsMethod(watersheds);

        log.info("Watersheds loaded.");
    }

    private World saveBasicWorld(World persistentWorld) {
        log.info("Loading the barren world.");
        World memoryWorld = persistentWorld.createBasicClone();

        saveWorldMethod(memoryWorld);

        log.info("Barren world loaded.");
        return memoryWorld;
    }
}

