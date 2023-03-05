package com.watchers.components.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.River;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.Watershed;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class RiverComputator {

    private final WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = worldRepository.getById(taskDto.getWorldId());
        Hibernate.initialize(world.getWatersheds());
        this.process(world);
        worldRepository.saveAndFlush(world);
    }

    protected void process(World world) {
        this.processUnconnectedRivers(world);
        this.checkWatershedIntegrity(world);
    }

    private void processUnconnectedRivers(World world) {
        List<Tile> riverTilesUnconnected = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getRiver() != null)
                .filter(tile -> tile.getRiver().getDownCurrentRiver() == null && !tile.getRiver().isRiverEnd())
                .collect(Collectors.toList());
        List<Tile> processedTiles = new ArrayList<>();

        int total = riverTilesUnconnected.size();
        int safetyToken = 0;
        while (!riverTilesUnconnected.isEmpty()) {
            if(safetyToken > 2000) {
                log.warn("ProcessUnconnectedRivers in loop for " + safetyToken + " loops");
            }
            if (safetyToken > total) {
                throw new RuntimeException("Looks like an infinite loop...");
            }

            Tile theChosenTile = riverTilesUnconnected.get(0);
            riverTilesUnconnected.remove(theChosenTile);

            Optional<Tile> flowDestination = theChosenTile.getCoordinate()
                    .getLowerOrEqualHeightLandCoordinatesWithinRange(1)
                    .stream()
                    .map(Coordinate::getTile)
                    .filter(neighbour -> noUpstreamTile(theChosenTile, neighbour))
                    .min(Comparator.comparing(Tile::getHeight));

            if (flowDestination.isPresent()){
                Tile destination = flowDestination.get();
                if (destination.isLand()){
                    destination.setWatershed(theChosenTile.getWatershed());
                    destination.setRiver(theChosenTile.getRiver());
                    if(!processedTiles.contains(destination)) {
                        riverTilesUnconnected.add(destination);
                        total++;
                    }
                } else {
                    // enters the ocean or sea
                    theChosenTile.getRiver().setRiverEnd(true);
                }
            } else {
                // river ends in a lake
                theChosenTile.setSurfaceType(SurfaceType.LAKE);
                theChosenTile.getRiver().setRiverEnd(true);
            }

            safetyToken++;
            processedTiles.add(theChosenTile);
        }
    }

    private static boolean noUpstreamTile(Tile currentTile, Tile neighbour){
        return !currentTile.getRiver()
                .getUpCurrentRivers()
                .contains(neighbour.getRiver());
    }

    private void checkWatershedIntegrity(World world){
        List<WatershedRiverStartingPoints> riverStartingPoints = world.getWatersheds().stream()
                .flatMap(watershed -> watershed.getRiverFlow().stream())
                .filter(river -> river.getUpCurrentRivers().isEmpty())
                .map(river -> new WatershedRiverStartingPoints(river.getWatershed(), river, 0L))
                .collect(Collectors.toList());

        riverStartingPoints.stream()
                .filter(this::riverIsLooping)
                .forEach(this::createLake);
    }

    private boolean riverIsLooping(WatershedRiverStartingPoints watershedRiverStartingPoints) {
        Set<Coordinate> riverLocations = new HashSet<>();
        River river = watershedRiverStartingPoints.riverStartingPoint;
        boolean endFound = false;
        boolean riverIsLooping = false;
        int total = watershedRiverStartingPoints.watershed.getRiverFlow().size();
        int safetyToken = 0;
        while (!endFound) {
            if(safetyToken > 2000) {
                log.warn("RiverIsLooping in loop for " + safetyToken + " loops");
            }
            if (river == null) {
                throw new RuntimeException("River was not ended correctly!");
            } else if (safetyToken > total || riverLocations.contains(river.getTile().getCoordinate())) {
                watershedRiverStartingPoints.lowestPoint = river.getTile().getHeight();
                riverIsLooping = true;
                endFound = true;
            } else if (!river.isRiverEnd() && river.getDownCurrentRiver() == null) {
                log.warn("River with no down current found that was not an end node");
                river.makeRiverFlowTillEnd();
                riverLocations.add(river.getTile().getCoordinate());
                endFound = river.isRiverEnd();
            } else {
                riverLocations.add(river.getTile().getCoordinate());
                endFound = river.isRiverEnd();
            }
            river = river.getDownCurrentRiver();
            safetyToken++;
        }

        return riverIsLooping;
    }

    private void createLake(WatershedRiverStartingPoints watershedRiverStartingPoints){
        log.warn("Creating lake from looping River");
        watershedRiverStartingPoints.watershed.getRiverFlow().stream()
                .map(River::getTile)
                .filter(tile -> tile.getHeight() <= watershedRiverStartingPoints.lowestPoint)
                .forEach(lakeTile -> {
                    lakeTile.setSurfaceType(SurfaceType.LAKE);
                    lakeTile.getRiver().setRiverEnd(true);
                });
    }

    @AllArgsConstructor
    private static class WatershedRiverStartingPoints {
        private Watershed watershed;
        private River riverStartingPoint;
        private Long lowestPoint;
    }
}
