package com.watchers.manager;

import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
class WorldFactory {

    private boolean lifePreSeeded;
    private int coastalZone;
    private int oceanicZone;
    private TileDefined tileDefined;

    public WorldFactory(@Value("${watch.lifePresSeeded}") boolean lifePreSeeded, @Value("${watch.coastalZone}") int coastalZone, @Value("${watch.oceanicZone}") int oceanicZone, TileDefined tileDefined){
        this.lifePreSeeded = lifePreSeeded;
        this.coastalZone = coastalZone;
        this.oceanicZone = oceanicZone;
        this.tileDefined = tileDefined;
    }

    World generateWorld(long xSize, long ySize, long continents){
        World world = new World(xSize, ySize);
        for (int i = 0; i < continents; i++) {
            Random random = new Random();
            boolean land = random.nextBoolean();
            Continent generatedContinent = new Continent(world, land ? SurfaceType.PLAIN : SurfaceType.OCEANIC);

            Coordinate startingCoordinate = generateStartingCoordinate(world, generatedContinent);

            generatedContinent.getCoordinates().add(startingCoordinate);
        }

        fillInWorld(world);
        world.fillTransactionals();

        specifyWaterZones(world);

        tileDefined.setStartingHeights(world);
        tileDefined.assignStartingType(world);

        if(lifePreSeeded) {
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
                    if(tile.getNeighboursWithinRange(Collections.singletonList(tile), coastalZone).stream().anyMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))){
                        tile.setSurfaceType(SurfaceType.COASTAL);
                    } else if(tile.getNeighboursWithinRange(Collections.singletonList(tile), oceanicZone).stream().noneMatch(streamTile -> SurfaceType.PLAIN.equals(streamTile.getSurfaceType()))){
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
