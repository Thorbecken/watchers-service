package com.watchers.manager;

import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.config.SettingConfiguration;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.environment.*;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldFactoryDTO;
import com.watchers.model.worldsetting.WorldSetting;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
class WorldFactory {

    private TileDefined tileDefined;
    private SettingConfiguration settingConfiguration;

    private static final long LONGITUDE_DIGREES = 360;
    private static final long LATHITUDE_DIGREES = 360;

    World generateWorld(long xSize, long ySize, long continents, WorldSetting worldSetting){
        World world = new World(xSize, ySize);
        world.setWorldSetting(worldSetting);
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

        createBasicClimateForWorld(world);

        return world;
    }

    private void createBasicClimateForWorld(World world) {
        List<Climate> climateList = createClimateLongitudeList(world);
        addLatitudeToClimates(climateList,world);
        calculateDistanceToEquator(climateList);
    }

    private void calculateDistanceToEquator(List<Climate> climateList) {
        long equator = LONGITUDE_DIGREES/2;
        climateList.forEach(
                climate -> climate.setDistanceToEquator(
                        climate.getLongitude()>equator?
                                climate.getLongitude()-equator
                                :equator-climate.getLongitude())
        );
    }

    private void addLatitudeToClimates(List<Climate> climateList, World world) {
        long centerY = (world.getYSize()-1)/2+1;
        long splitterX = world.getXSize()/2;

        Map<Long, List<Climate>>  sortedNorthernClimates = climateList.stream()
                .filter(climate -> climate.getCoordinate().getXCoord() <= splitterX)
                .collect(Collectors.groupingBy(Climate::getLongitude));

        Map<Long, List<Climate>>  sortedSouthernClimates = climateList.stream()
                .filter(climate -> climate.getCoordinate().getXCoord() > splitterX)
                .collect(Collectors.groupingBy(Climate::getLongitude));

        sortedNorthernClimates.keySet()
                .forEach(key -> assingNothernLatitude(sortedNorthernClimates.get(key), key, centerY));

        sortedNorthernClimates.keySet()
                .forEach(key -> assignSouthernLatitude(sortedSouthernClimates.get(key), key, centerY));

    }

    void assingNothernLatitude(List<Climate> climates, Long longitude, long centerY){
        World world = climates.get(0).getCoordinate().getWorld();
        boolean down = false;
        boolean left = true;
        boolean right = false;

        Coordinate startingCoordinate = climates.stream()
                .map(Climate::getCoordinate)
                .filter(coordinate -> coordinate.getYCoord() == centerY-longitude)
                .max(Comparator.comparing(Coordinate::getXCoord))
                .get();

        long longitudeRingSize = climates.size();

        for (long i = 0; i < longitudeRingSize; i++) {
            startingCoordinate.getClimate().setLatitude(i/longitudeRingSize*LATHITUDE_DIGREES);
            if(left){
                if(startingCoordinate.getXCoord()+1<=longitude+centerY) {
                    startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord() + 1, startingCoordinate.getYCoord());
                } else {
                    startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord()-1);
                    right = false;
                    down = true;
                }
            } else if(down){
                if(startingCoordinate.getYCoord()-1 >= longitude-centerY ) {
                    startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord() - 1);
                } else {
                    startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord()-1, startingCoordinate.getYCoord());
                    down = false;
                    left = true;
                }
            } else if(right){
                if(startingCoordinate.getXCoord()-1 >= longitude-centerY) {
                    startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord() - 1, startingCoordinate.getYCoord());
                } else {
                    startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord()+1);
                    left = false;
                }
            } else {
                startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord()+1);
            }
        }

    }

    void assignSouthernLatitude(List<Climate> climates, Long longitude, long centerY){
        World world = climates.get(0).getCoordinate().getWorld();
        boolean down = false;
        boolean left = false;
        boolean right = true;

        Coordinate startingCoordinate = climates.stream()
                .map(Climate::getCoordinate)
                .filter(coordinate -> coordinate.getYCoord() == centerY-longitude)
                .min(Comparator.comparing(Coordinate::getXCoord))
                .get();

        long longitudeRingSize = climates.size();

        for (long i = 0; i < longitudeRingSize; i++) {
          startingCoordinate.getClimate().setLatitude(i/longitudeRingSize*LATHITUDE_DIGREES);
          if(right){
              if(startingCoordinate.getXCoord()+1<=longitude+centerY) {
                  startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord() + 1, startingCoordinate.getYCoord());
              } else {
                  startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord()-1);
                  right = false;
                  down = true;
              }
          } else if(down){
              if(startingCoordinate.getYCoord()-1 >= longitude-centerY ) {
                  startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord() - 1);
              } else {
                  startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord()-1, startingCoordinate.getYCoord());
                  down = false;
                  left = true;
              }
          } else if(left){
              if(startingCoordinate.getXCoord()-1 >= longitude-centerY) {
                  startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord() - 1, startingCoordinate.getYCoord());
              } else {
                  startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord()+1);
                  left = false;
              }
          } else {
              startingCoordinate = world.getCoordinate(startingCoordinate.getXCoord(), startingCoordinate.getYCoord()+1);
          }
        }
    }

    private List<Climate> createClimateLongitudeList(World world) {
        List<Climate> climateList = new ArrayList<>();
        long splitterX = world.getXSize()/2;
        long centerX = (splitterX-1)/2+1;
        long centerY = (world.getYSize()-1)/2+1;
        long northernCenterX = centerX;
        long southernCenterX = centerX+splitterX;

        world.getCoordinates().parallelStream()
                .filter(coordinate -> coordinate.getXCoord() <= splitterX)
                .forEach(
                        coordinate -> {
                            Climate climate = new Climate();
                            climate.setLongitude(getLongitude(coordinate, northernCenterX, centerY));
                            climate.setCoordinate(coordinate);
                            climateList.add(climate);
                        }
                );

        world.getCoordinates().parallelStream()
                .filter(coordinate -> coordinate.getXCoord() > splitterX)
                .forEach(
                        coordinate -> {
                            Climate climate = new Climate();
                            climate.setLongitude(getLongitude(coordinate, southernCenterX, centerY));
                            climate.setCoordinate(coordinate);
                            climateList.add(climate);
                        }
                );

        return climateList;
    }

    private long getLongitude(Coordinate coordinate, long centerX, long centerY) {
        long xCoord = coordinate.getXCoord();
        long yCoord = coordinate.getYCoord();
        long xDifference = xCoord>centerX?xCoord-centerX:centerX-xCoord;
        long yDifference = yCoord>centerY?yCoord-centerY:centerY-yCoord;
        long rawLongitude = xDifference>yDifference?xDifference:yDifference;
        return rawLongitude/coordinate.getWorld().getXSize()*LONGITUDE_DIGREES;
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
