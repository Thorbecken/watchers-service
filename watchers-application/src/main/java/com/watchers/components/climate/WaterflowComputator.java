package com.watchers.components.climate;

import com.watchers.components.continentaldrift.TileDefined;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Lake;
import com.watchers.model.environment.Tile;
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
public class WaterflowComputator {

    private final TileDefined tileDefined;

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
        worldTiles.forEach(Tile::resetDownflowValues);
        tileDefined.assignStartingType(world);

//        2. create a list of all land tiles, ordered by height
        List<Tile> landTileList = worldTiles.stream()
                .filter(Tile::isLand)
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
                tile.setDownWardTile(downwardTile);
                if (downwardTile.isSea()) {
                    tile.setCoastalLand(true);
                } else {
                    downwardTile.getUpwardTiles().add(tile);
                }
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
            downFlowMap.forEach(Tile::setDownWardTile);

            noChanges = tilesWithDownwardTileNeighbour.isEmpty();
        }

//        6. create lakes from tiles that can not get a down flowing tile
        List<Lake> lakes = new ArrayList<>();
        List<Tile> largeLakeTiles = landTileList.stream()
                .filter(tile -> !tile.isLakeTile()
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

            allLargeLakeTilesHaveBeenAssigned = largeLakeTiles.stream()
                    .noneMatch(tile -> tile.getLake() == null);
        }


//        7. assign water flow for tiles that have down flowing tiles but no up current tiles
        landTileList.stream()
                .filter(tile -> tile.getUpwardTiles().isEmpty())
                .forEach(tile -> tile.setDownFlowAmount(tile.getLandMoisture()));

//        8. assign water flow for tiles that have not been assigned and where all up current tiles have been assigned
        boolean allWaterHasFlownDown = landTileList.stream()
                .noneMatch(Tile::readyToFlow);
        int riverCounter = 0;

        while (!allWaterHasFlownDown) {
            log.debug("River loop counter, round: " + ++riverCounter);
            List<Tile> nextDownStream = landTileList.stream()
                    .filter(Tile::readyToFlow)
                    .collect(Collectors.toList());

            for (Tile tile : nextDownStream) {
                double inflow = tile.getUpwardTiles().stream()
                        .mapToDouble(Tile::getDownFlowAmount)
                        .sum();
                tile.setDownFlowAmount(inflow + tile.getLandMoisture());
            }

            allWaterHasFlownDown = nextDownStream.isEmpty();
        }

//        9. calculate mean water level of lakes
        for (Lake lake : lakes) {
            double totalDownflow = lake.getLakeTiles().stream()
                    .mapToDouble(Tile::getDownFlowAmount)
                    .sum();

            lake.setMeanLakeHeight(totalDownflow / lake.getLakeTiles().size());
        }

//        10. assign markers of rivers and lakes
        worldTiles.stream()
                .filter(tile -> tile.getLake() != null
                        && tile.getLake().getMeanLakeHeight() > 5d)
                .forEach(tile -> {
                    tile.setLakeTile(true);
                    tile.setSurfaceType(SurfaceType.LAKE);
                });

        worldTiles.stream()
                .filter(tile -> tile.getDownFlowAmount() >= 10d
                        && tile.getDownFlowAmount() < 50d)
                .forEach(tile -> tile.setRiver(true));

        worldTiles.stream()
                .filter(tile -> tile.getDownFlowAmount() >= 50d)
                .forEach(tile -> {
                    tile.setRiver(true);
                    tile.setLargeRiver(true);
                    tile.setSurfaceType(SurfaceType.LARGE_RIVER);
                });
    }
}
