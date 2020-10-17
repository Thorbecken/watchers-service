package com.watchers.manager;

import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.config.SettingConfiguration;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
class WorldFactory {

    private TileDefined tileDefined;
    private SettingConfiguration settingConfiguration;

    World generateWorld(long xSize, long ySize, long continents){
        World world = new World(xSize, ySize);
        int continental = 0;
        int oceeanic = 0;
        for (int i = 0; i < continents; i++) {
            SurfaceType surfaceType;
            if(oceeanic * settingConfiguration.getContinentalToOcceanicRatio() >= continental){
                surfaceType = SurfaceType.PLAIN;
                continental++;
            } else {
                surfaceType = SurfaceType.OCEANIC;
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

        if(settingConfiguration.isLifePreSeeded()) {
            world.getContinents().forEach(continent -> world.getCoordinates().stream()
                    .filter(coordinate -> coordinate.getContinent() == continent)
                    .findFirst()
                    .ifPresent(LifeManager::seedLife));

        }

        return world;
    }

    private void specifyWaterZones(World world) {
        log.info("sepperating the oceans");
        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> SurfaceType.OCEANIC.equals(tile.getSurfaceType()))
                .forEach(
                tile -> {
                    if(tile.getNeighboursWithinRange(Collections.singletonList(tile), settingConfiguration.getCoastalZone()).stream().anyMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))){
                        tile.setSurfaceType(SurfaceType.COASTAL);
                    } else if(tile.getNeighboursWithinRange(Collections.singletonList(tile), settingConfiguration.getOceanicZone()).stream().noneMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))){
                        tile.setSurfaceType(SurfaceType.DEEP_OCEAN);
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

        while(dto.getOpenCoordinates().size() >= 1){
            log.debug("In loop: " + dto.getOpenCoordinates().size() + " coordinates left");
            mockContinents.stream()
                    .filter(mockContinent -> !CollectionUtils.isEmpty(mockContinent.getPossibleCoordinates()))
                    .forEach(
                        mockContinent -> mockContinent.addRandomCoordinate(dto)
            );
        }

        log.info("Coordinates assigned to continents.");

        world.getCoordinates().clear();
        world.getContinents().removeIf(continent -> continent.getType() == null);

        mockContinents.forEach(MockContinent::generateContinent);
    }

    private Coordinate generateStartingCoordinate(World world, Continent continent) {
        long xCoord =  new RandomDataGenerator().nextLong(1, world.getXSize());
        long yCoord =  new RandomDataGenerator().nextLong(1, world.getYSize());
        Coordinate startingCoordinate = new Coordinate(xCoord, yCoord, world, continent);
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
