package com.watchers.manager;

import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.model.environment.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
class WorldFactory {

    private int coastalZone;
    private int oceanicZone;
    private TileDefined tileDefined;

    public WorldFactory(@Value("${watch.coastalZone}") int coastalZone, @Value("${watch.oceanicZone}") int oceanicZone, TileDefined tileDefined){
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

            Tile startingTile = generateStartingTile(world, generatedContinent);

            generatedContinent.getTiles().add(startingTile);
            world.getContinents().add(generatedContinent);
        }

        fillInWorld(world);
        world.fillTransactionals();

        specifyWaterZones(world);
        tileDefined.process(world);

        return world;
    }

    private void specifyWaterZones(World world) {
        System.out.println("sepperating the oceans");
        world.getTiles().stream()
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
        System.out.println("Oceans are sepperated");
    }

    private void fillInWorld(World world) {
        WorldFactoryDTO dto = new WorldFactoryDTO(world);
        List<MockContinent> mockContinents = new ArrayList<>();
        world.getContinents().forEach(
                continent -> mockContinents.add(new MockContinent(continent))
                );

        while(dto.getOpenCoordinates().size() >= 1){
            System.out.println("In loop: " + dto.getOpenCoordinates().size() + " coordinates left");
            mockContinents.stream()
                    .filter(mockContinent -> !CollectionUtils.isEmpty(mockContinent.getPossibleCoordinates()))
                    .forEach(
                        mockContinent -> mockContinent.addRandomCoordinate(dto)
            );
        }

        System.out.println("Left the loop");

        // opschonen lijsten
        world.setTiles(new HashSet<>());

        // invullen lijsten
        mockContinents.forEach(
            mockContinent -> {
                       Continent continent = mockContinent.generateContinent();
                       world.getTiles().addAll(continent.getTiles());
                       //mockContinent.getContinent().getCoordinates().addAll(mockContinent.getCoordinates());
                   }
        );
    }

    private Tile generateStartingTile(World world, Continent continent) {
        long xCoord =  new RandomDataGenerator().nextLong(1, world.getXSize());
        long yCoord =  new RandomDataGenerator().nextLong(1, world.getYSize());
        Tile startingTile = new Tile(xCoord, yCoord, world, continent);
        if (world.getContinents().stream().anyMatch(
                continent1 -> continent.getTiles().stream().anyMatch(
                        startingTile::coordinateEquals
                )
        )) {
           return generateStartingTile(world, continent);
        } else {
            return startingTile;
        }
    }
}
