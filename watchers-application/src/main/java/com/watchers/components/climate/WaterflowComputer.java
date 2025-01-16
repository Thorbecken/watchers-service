package com.watchers.components.climate;

import com.watchers.components.Computer;
import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Lake;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.crystal.AquiferCrystal;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/*
inspirations:
https://www.researchgate.net/figure/D8-algorithm-for-determining-the-flow-direction-and-flow-accumulation_fig1_348667327
https://developers.arcgis.com/rest/services-reference/enterprise/flow-direction.htm
 */
@Slf4j
@Component
@AllArgsConstructor
public class WaterflowComputer implements Computer {

    private final TileDefined tileDefined;
    protected static final double LAKE_THRESHOLD = 0.0d;
    protected static final double RIVER_THRESHOLD = 10d;
    protected static final double LARGE_RIVER_THRESHOLD = 100d;
    private static final double UNDERWATER_RIVER_THRESHOLD = 50d;

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = taskDto.getWorld();
        this.process(world);
    }

    /*
    Process to determine the flow of water in the world

    1. reset all the values
    2. create a list of all land tiles, ordered by height
    3. make all land tiles that are the lowest points into a lake
    4. calculate all down flowing tiles that have a lower neighbour
    5. calculate down flowing tiles for land tiles that have no lower neighbour
    that are not lakes
     and by letting them flow to a neighbour that has a down flowing tile
     6. create lakes from tiles that can not get a down flowing tile
     7. assign water flow for tiles that have down flowing tiles but no up current tiles
     8. assign water flow for tiles that have not been assigned and where all up current tiles have been assigned
     9. calculate mean water level of lakes
     10. assign markers of rivers and lakes
     */
    public void process(World world) {
//        1. reset all the values
        List<Tile> worldTiles = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .collect(Collectors.toList());
        worldTiles.forEach(Tile::resetWaterValues);
        tileDefined.assignStartingType(world);

        worldTiles.stream()
                .filter(tile -> tile.getPointOfInterest() instanceof AquiferCrystal)
                .forEach(aquiferTile -> this.processAquiferTile(aquiferTile, worldTiles));

//        2. create a lists of all land and water tiles, ordered by height
        List<Tile> landTileList = worldTiles.stream()
                .filter(Tile::isLand)
                .sorted(Comparator.comparing(Tile::getHeight).reversed())
                .collect(Collectors.toList());
        List<Tile> waterTileList = worldTiles.stream()
                .filter(Tile::isWater)
                .sorted(Comparator.comparing(Tile::getHeight).reversed())
                .collect(Collectors.toList());

//        3. make all land tiles that are the lowest points into a lake
        landTileList.stream()
                .filter(Tile::isLowestPoint)
                .forEach(lakeTile -> lakeTile.setLakeTile(true));

//        4. calculate all down flowing tiles that have a lower neighbour
        List<Tile> downFlowingTiles = landTileList.stream()
                .filter(tile -> !tile.isLakeTile()
                        && tile.hasLowerHeightNeighbour())
                .collect(Collectors.toList());
        for (Tile tile : downFlowingTiles) {
            List<Tile> lowerHeightTilesOrderedByHeightDescending = tile.getLowerHeightTilesOrderedByHeightDescending();
            if (!lowerHeightTilesOrderedByHeightDescending.isEmpty()) {
                Tile downwardTile = tile.getLowerHeightTilesOrderedByHeightDescending().get(0);
                tile.setDownWardTileAndSetFlowDirection(downwardTile);
            }
        }

//        5. calculate down flowing tiles for tiles that have no lower neighbour
//        that are not lakes
//        and by letting them flow to a neighbour that has a down flowing tile
        boolean noChanges = false;
        while (!noChanges) {
            List<Tile> tilesWithDownwardTileNeighbour = landTileList.stream()
                    .filter(tile -> !tile.isLakeTile()
                            && tile.getDownWardTile() == null)
                    .collect(Collectors.toList());

            // assigning of downward tile is set apart from calculating as to not interfere with the calculations
            Map<Tile, Tile> downFlowMap = new HashMap<>();
            for (Tile tile : tilesWithDownwardTileNeighbour) {
                tile.getNeighbours().stream()
                        .filter(neighbour -> neighbour.getDownWardTile() != null)
                        .findFirst()
                        .ifPresent(target -> downFlowMap.put(tile, target));
            }

            downFlowMap.forEach(Tile::setDownWardTileAndSetFlowDirection);

            noChanges = tilesWithDownwardTileNeighbour.isEmpty();
        }

//        6. create lakes from tiles that can not get a down flowing tile
        List<Lake> lakes = this.createLakes(landTileList);


//        7. assign water flow for tiles that have down flowing tiles but no up current tiles
//           These are the tiles at which the water streams start.
        landTileList.stream()
                .filter(tile -> tile.getUpwardTiles().isEmpty())
                .forEach(Tile::processMovementOfWater);

//        8. assign water flow for tiles that have not been assigned and where all up current tiles have been assigned
        this.assignWaterFlow(landTileList);
        this.assignUnderWaterWaterFlow(waterTileList);

//        9. calculate mean water level of lakes
        this.calculateMeanWaterLevelOfLakes(lakes);

//        10. assign markers of rivers and lakes
        this.assignRiverAndLakeMarkers(landTileList);
    }

    private List<Lake> createLakes(List<Tile> landTileList) {
        List<Lake> lakes = new ArrayList<>();
        List<Tile> largeLakeTiles = landTileList.stream()
                .filter(tile -> tile.isLakeTile()
                        && tile.getDownWardTile() == null)
                .collect(Collectors.toList());
        boolean allLargeLakeTilesHaveBeenAssigned = largeLakeTiles.stream()
                .noneMatch(tile -> tile.getLake() == null);
        int largeLakeCounter = 0;
        while (!allLargeLakeTilesHaveBeenAssigned) {
            log.debug("Large lake loop, round number: " + ++largeLakeCounter);
            Lake lake = new Lake();
            lakes.add(lake);
            Tile lakeStartingTile = largeLakeTiles.stream()
                    .filter(tile -> tile.getLake() == null)
                    .findFirst()
                    .orElseThrow();
            Set<Tile> lakeTiles = new HashSet<>();
            lakeTiles.add(lakeStartingTile);
            lake.getLakeTiles().add(lakeStartingTile);

            boolean lakeIsComplete = false;
            while (!lakeIsComplete) {
                List<Tile> newLakeTiles = lake.getLakeTiles().stream()
                        .flatMap(tile -> tile.getSameHeightNeigbours().stream())
                        .filter(neighbour -> !lakeTiles.contains(neighbour))
                        .collect(Collectors.toList());

                lakeTiles.addAll(newLakeTiles);

                lakeIsComplete = newLakeTiles.isEmpty();
            }
            lake.setLakeTiles(lakeTiles);
            lakeTiles.forEach(tile -> tile.setLake(lake));

            allLargeLakeTilesHaveBeenAssigned = largeLakeTiles.stream()
                    .noneMatch(tile -> tile.getLake() == null);
        }
        return lakes;
    }

    private void assignWaterFlow(List<Tile> landTileList) {
        boolean allWaterHasFlownDown = landTileList.stream()
                .noneMatch(Tile::readyToFlow);
        int riverCounter = 0;

        while (!allWaterHasFlownDown) {
            log.debug("River loop counter, round: " + ++riverCounter);
            landTileList.stream()
                    .filter(Tile::readyToFlow)
                    .forEach(Tile::processMovementOfWater);

            allWaterHasFlownDown = landTileList.stream()
                    .noneMatch(Tile::readyToFlow);
        }
    }

    private void assignUnderWaterWaterFlow(List<Tile> waterTileList) {
        Set<Tile> processedTiles = new HashSet<>();
        boolean tilesInNeedOfProcessing = true;
        while (tilesInNeedOfProcessing) {
            List<Tile> underwaterFlowingTiles = waterTileList.stream()
                    .filter(tile -> tile.getSurfaceWater() >= UNDERWATER_RIVER_THRESHOLD)
                    .filter(tile -> !processedTiles.contains(tile))
                    .collect(Collectors.toList());
            tilesInNeedOfProcessing = !underwaterFlowingTiles.isEmpty();
            processedTiles.addAll(underwaterFlowingTiles);

            underwaterFlowingTiles.forEach(tile -> {
                List<Tile> lowerTiles = tile.getLowerHeightTilesOrderedByHeightDescending();
                lowerTiles.stream()
                        .filter(lowerTile -> lowerTile.getSurfaceWater() >= 0)
                        .min(Comparator.comparing(Tile::getHeight))
                        .ifPresent(lowerTile -> {
                            long heightDifference = tile.getHeight() - lowerTile.getHeight();
                            double lowestValueOfWaterOrHeight = Math.min(tile.getSurfaceWater(), heightDifference);
                            lowerTile.setSurfaceWater(lowestValueOfWaterOrHeight);
                            tile.setDownWardTile(lowerTile);
                        });
            });
        }
    }

    private void calculateMeanWaterLevelOfLakes(List<Lake> lakes) {
        for (Lake lake : lakes) {
            double totalSurfaceWater = lake.getLakeTiles().stream()
                    .mapToDouble(Tile::getSurfaceWater)
                    .sum();

            double lakeSize = lake.getLakeTiles().size();
            double lakeHeight = totalSurfaceWater / lakeSize;
            lake.setMeanLakeHeight(lakeHeight);
            lake.getLakeTiles().forEach(tile -> tile.setSurfaceWater(lakeHeight));
        }
    }

    private void assignRiverAndLakeMarkers(List<Tile> landTileList) {
        landTileList.stream()
                .filter(tile -> tile.getLake() != null
                        && tile.getLake().getMeanLakeHeight() > LAKE_THRESHOLD)
                .forEach(tile -> tile.setSurfaceType(SurfaceType.LAKE));

        landTileList.stream()
                .filter(tile -> !tile.isLakeTile())
                .filter(tile -> tile.getSurfaceWater() >= RIVER_THRESHOLD
                        && tile.getSurfaceWater() < LARGE_RIVER_THRESHOLD)
                .forEach(tile -> tile.setRiver(true));

        landTileList.stream()
                .filter(tile -> !tile.isLakeTile())
                .filter(tile -> tile.getSurfaceWater() >= LARGE_RIVER_THRESHOLD)
                .forEach(tile -> {
                    tile.setRiver(true);
                    tile.setLargeRiver(true);
                    tile.setSurfaceType(SurfaceType.LARGE_RIVER);
                });
    }

    private void processAquiferTile(Tile aquiferTile, List<Tile> worldTiles) {
        worldTiles.stream()
                .filter(tile -> tile.getDistance(aquiferTile) <= 4)
                .forEach(tile -> tile.addRainfall(RIVER_THRESHOLD));
        worldTiles.stream()
                .filter(tile -> tile.getDistance(aquiferTile) <= 1)
                .forEach(tile -> tile.addRainfall(LARGE_RIVER_THRESHOLD));
    }
}
