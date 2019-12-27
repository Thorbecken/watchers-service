package com.watchers.manager;

import com.watchers.model.environment.*;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.*;

class WorldFactory {

    World generateWorld(long xSize, long ySize, long continents){
        World world = new World(xSize, ySize);
        for (int i = 0; i < continents; i++) {
            Random random = new Random();
            boolean land = random.nextBoolean();
            Continent generatedContinent = new Continent(world, land ? SurfaceType.CONTINENTAL : SurfaceType.OCEANIC);

            Tile startingTile = generateStartingTile(world, generatedContinent);

            generatedContinent.getTiles().add(startingTile);
            world.getContinents().add(generatedContinent);
        }

        fillInWorld(world);

        return world;
    }

    private void fillInWorld(World world) {
        WorldFactoryDTO dto = new WorldFactoryDTO(world);
        List<MockContinent> mockContinents = new ArrayList<>();
        world.getContinents().forEach(
                continent -> mockContinents.add(new MockContinent(continent))
                );

        mockContinents.forEach(
                mockContinent -> dto.getTakenTiles().addAll(mockContinent.getTiles())
        );

        while(dto.getOpenTiles().size() >= 1){
            System.out.println("In loop: " + dto.getOpenTiles().size() + " tiles left");
            mockContinents.forEach(
                    mockContinent -> mockContinent.addRandomTile(dto)
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
                       //mockContinent.getContinent().getTiles().addAll(mockContinent.getTiles());
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
