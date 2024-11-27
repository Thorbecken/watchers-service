package com.watchers.manager;

import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.helper.ClimateHelper;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.dto.MockContinent;
import com.watchers.model.dto.WorldFactoryDTO;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Flora;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.life.GreatFlora;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.model.world.WorldSettings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
class WorldFactory {

    private final TileDefined tileDefined;

    World generateWorld(WorldSettings worldSettings, WorldMetaData worldMetaData) {
        World world = new World(worldSettings.getXSize(), worldSettings.getYSize());
        world.setWorldMetaData(worldMetaData);
        world.setWorldSettings(worldSettings);
        worldSettings.setWorld(world);
        worldMetaData.setWorld(world);
        worldMetaData.setXSize(world.getXSize());
        worldMetaData.setYSize(world.getYSize());

        int continental = 0;
        int oceeanic = 0;
        for (int i = 0; i < worldSettings.getNumberOfContinents(); i++) {
            SurfaceType surfaceType;
            if (oceeanic * worldSettings.getContinentalToOcceanicRatio() >= continental) {
                surfaceType = SurfaceType.PLAIN;
                continental++;
            } else {
                surfaceType = SurfaceType.OCEAN;
                oceeanic++;
            }
            Continent generatedContinent = new Continent(world, surfaceType);

            Coordinate startingCoordinate = generateStartingCoordinate(world, generatedContinent);

            generatedContinent.getCoordinates().add(startingCoordinate);
        }

        fillInWorld(world);
        world.fillTransactionals();

        specifyWaterZones(world);

        tileDefined.setStartingHeights(world);
        tileDefined.assignStartingType(world);

        log.info("Sepperating the skies");
        ClimateHelper.calculateAndWeaveAirflows(world);
        log.info("Skies are sepperated");

        if (worldSettings.isLifePreSeeded()) {
            world.getContinents().forEach(continent -> world.getCoordinates().stream()
                    .filter(coordinate -> coordinate.getContinent() == continent)
                    .findFirst()
                    .ifPresent(LifeManager::seedLife));

            world.getContinents().stream()
                    .filter(continent -> !continent.getType().equals(SurfaceType.OCEAN))
                    .map(CoordinateHelper::getMeanCoordinate)
                    .forEach(GreatFlora::new);

            world.getContinents().stream()
                    .filter(continent -> continent.getType().equals(SurfaceType.OCEAN))
                    .flatMap(continent -> continent.getCoordinates().stream())
                    .map(Coordinate::getTile)
                    .map(Tile::getBiome)
                    .forEach(biome -> biome.setTreeFlora(Flora.getSeawaterFlora(biome.getTile().getCoordinate().getClimate().getMeanTemperature())));

            log.info("Pre seeded the world with life");
        }

        return world;
    }

    private void specifyWaterZones(World world) {
        log.info("Sepperating the oceans");
        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> SurfaceType.SEA.equals(tile.getSurfaceType()))
                .forEach(
                        tile -> {
                            if (tile.getNeighboursWithinRange(Collections.singletonList(tile), world.getWorldSettings().getCoastalZone()).stream().anyMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))) {
                                tile.setSurfaceType(SurfaceType.COASTAL);
                            } else if (tile.getNeighboursWithinRange(Collections.singletonList(tile), world.getWorldSettings().getOceanicZone()).stream().noneMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))) {
                                tile.setSurfaceType(SurfaceType.OCEAN);
                            }
                        }
                );
        log.info("Oceans are sepperated");
    }

    private void fillInWorld(World world) {
        WorldFactoryDTO dto = new WorldFactoryDTO(world);
        List<MockContinent> mockContinents = world.getContinents().stream()
                .map(MockContinent::new)
                .collect(Collectors.toList());

        long numberOfCoordinateToSplit = world.getXSize() * world.getYSize();
        log.info("Separating the earth.");

        int counter = 0;
        Queue<Coordinate> openCoordinates = new LinkedList<>(dto.getOpenCoordinates());

        while (!openCoordinates.isEmpty()) {
            if (++counter == 25) {
                counter = 0;
                double percentageDone = 1 - (double) openCoordinates.size() / (double) numberOfCoordinateToSplit;
                log.info("Separated " + NumberFormat.getPercentInstance().format(percentageDone) + " of the earth.");
            }

            // Randomly shuffle the continents to ensure fair distribution
            Collections.shuffle(mockContinents);

            // Process each continent
            for (MockContinent mockContinent : mockContinents) {
                Set<Coordinate> possibleCoordinates = mockContinent.getPossibleCoordinates();
                if (!possibleCoordinates.isEmpty() && !openCoordinates.isEmpty()) {
                    // Start with a random coordinate from the open coordinates
                    Coordinate openCoordinate = openCoordinates.poll(); // Get and remove the head of the queue
                    if (openCoordinate != null) {
                        if (possibleCoordinates.contains(openCoordinate)) {
                            mockContinent.getCoordinates().add(openCoordinate);
                            mockContinent.getPossibleCoordinates().addAll(openCoordinate.getNeighbours());
                            mockContinent.getCoordinates().forEach(mockContinent.getPossibleCoordinates()::remove);
                        } else {
                            openCoordinates.add(openCoordinate);
                        }
                    }
                }
            }
        }

        log.info("Earth is separated");

        world.getCoordinates().clear();
        world.getContinents().removeIf(continent -> continent.getType() == null);

        mockContinents.forEach(mockContinent -> mockContinent.generateContinent(world));
    }

    private Coordinate generateStartingCoordinate(World world, Continent continent) {
        long xCoord = new RandomDataGenerator().nextLong(1, world.getXSize());
        long yCoord = new RandomDataGenerator().nextLong(1, world.getYSize());
        Coordinate startingCoordinate = CoordinateFactory.createCoordinate(xCoord, yCoord, world, continent);
        if (world.getContinents().stream().anyMatch(
                continent1 -> continent.getCoordinates().stream().anyMatch(
                        startingCoordinate::equals
                )
        )) {
            return generateStartingCoordinate(world, continent);
        } else {
            return startingCoordinate;
        }
    }
}