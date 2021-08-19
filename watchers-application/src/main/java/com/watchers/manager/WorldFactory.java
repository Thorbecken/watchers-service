package com.watchers.manager;

import com.watchers.components.climate.TemperatureZoneComputator;
import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.helper.SkyHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.dto.MockContinent;
import com.watchers.model.dto.WorldFactoryDTO;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldMetaData;
import com.watchers.model.world.WorldSettings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
class WorldFactory {

    private final TileDefined tileDefined;
    private final TemperatureZoneComputator temperatureZoneComputator;

    World generateWorld(WorldSettings worldSettings, WorldMetaData worldMetaData){
        World world = new World(worldSettings.getXSize(), worldSettings.getYSize());
        world.setWorldMetaData(worldMetaData);
        world.setWorldSettings(worldSettings);
        worldSettings.setWorld(world);
        worldMetaData.setWorld(world);

        int continental = 0;
        int oceeanic = 0;
        for (int i = 0; i < worldSettings.getNumberOfContinents(); i++) {
            SurfaceType surfaceType;
            if(oceeanic * worldSettings.getContinentalToOcceanicRatio() >= continental){
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
        SkyHelper.calculateAirflows(world);
        log.info("Skies are sepperated");

        if(worldSettings.isLifePreSeeded()) {
            world.getContinents().forEach(continent -> world.getCoordinates().stream()
                    .filter(coordinate -> coordinate.getContinent() == continent)
                    .findFirst()
                    .ifPresent(LifeManager::seedLife));

            log.info("Pre seeded the world with life");
        }

        temperatureZoneComputator.process(world);

        return world;
    }

    private void specifyWaterZones(World world) {
        log.info("Sepperating the oceans");
        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> SurfaceType.SEA.equals(tile.getSurfaceType()))
                .forEach(
                tile -> {
                    if(tile.getNeighboursWithinRange(Collections.singletonList(tile), world.getWorldSettings().getCoastalZone()).stream().anyMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))){
                        tile.setSurfaceType(SurfaceType.COASTAL);
                    } else if(tile.getNeighboursWithinRange(Collections.singletonList(tile), world.getWorldSettings().getOceanicZone()).stream().noneMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))){
                        tile.setSurfaceType(SurfaceType.OCEAN);
                    }
                }
        );
        log.info("Oceans are sepperated");
    }

    private void fillInWorld(World world) {
        WorldFactoryDTO dto = new WorldFactoryDTO(world);
        List<MockContinent> mockContinents = new ArrayList<>();
        world.getContinents().forEach(
                continent -> mockContinents.add(new MockContinent(continent, world))
                );

        long numberOfCoordinateToSplit = world.getXSize() * world.getYSize();
        log.info("Sepperating the earth.");

        int counter = 0;
        while(dto.getOpenCoordinates().size() >= 1){
            if(++counter == 25) {
                counter = 0;
                double percentageDone = 1 - (double) dto.getOpenCoordinates().size() / (double) numberOfCoordinateToSplit;
                log.info("Sepperated " + NumberFormat.getPercentInstance().format(percentageDone) + " of the earth.");
            }

            mockContinents.stream()
                    .filter(mockContinent -> !CollectionUtils.isEmpty(mockContinent.getPossibleCoordinates()))
                    .forEach(
                        mockContinent -> mockContinent.addRandomCoordinate(dto)
            );
        }

        log.info("Earth is sepperated");

        world.getCoordinates().clear();
        world.getContinents().removeIf(continent -> continent.getType() == null);

        mockContinents.forEach(MockContinent::generateContinent);
    }

    private Coordinate generateStartingCoordinate(World world, Continent continent) {
        long xCoord =  new RandomDataGenerator().nextLong(1, world.getXSize());
        long yCoord =  new RandomDataGenerator().nextLong(1, world.getYSize());
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
