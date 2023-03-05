package com.watchers.components.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.River;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.Watershed;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class WatershedComputator {

    private final WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = worldRepository.getById(taskDto.getWorldId());
        Hibernate.initialize(world);
        this.process(world);
        worldRepository.saveAndFlush(world);
    }

    protected void process(World world) {
        Hibernate.initialize(world.getWatersheds());
        List<Tile> tilesWithPrecipitationAndNoWatershed = world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getWatershed() == null)
                .filter(tile -> tile.getLandMoisture() > 0)
                .collect(Collectors.toList());

        int total = tilesWithPrecipitationAndNoWatershed.size();
        int safetyToken = 0;
        while (!tilesWithPrecipitationAndNoWatershed.isEmpty()) {
            if (safetyToken > total) {
                throw new RuntimeException("Looks like an infinite loop...");
            }


            Tile theChosenTile = tilesWithPrecipitationAndNoWatershed.get(0);
            Optional<Watershed> watershed = getClosestWatershed(theChosenTile, 2);
            if (watershed.isPresent()) {
                Optional<Watershed> closerWatershed = getClosestWatershed(theChosenTile, 1);
                if (closerWatershed.isPresent()) {
                    closerWatershed.get().addTile(theChosenTile);
                    tilesWithPrecipitationAndNoWatershed.remove(theChosenTile);
                    return;
                }
                watershed.get().addTile(theChosenTile);
                River river = new River(watershed.get());
                theChosenTile.setRiver(river);
                tilesWithPrecipitationAndNoWatershed.remove(theChosenTile);
                return;
            } else {
                Watershed newWatershed = new Watershed(world);
                newWatershed.addTile(theChosenTile);
                River river = new River(newWatershed);
                theChosenTile.setRiver(river);
                tilesWithPrecipitationAndNoWatershed.remove(theChosenTile);
            }

            safetyToken++;
        }
    }

    private Optional<Watershed> getClosestWatershed(Tile tile, int distance) {
        return tile.getCoordinate().getLowerOrEqualHeightLandCoordinatesWithinRange(distance).stream()
                .map(Coordinate::getTile)
                .map(TileWatershedHolder::new)
                .filter(TileWatershedHolder::hasWatershed)
                .reduce((TileWatershedHolder x, TileWatershedHolder y) -> {
                    if (x.getTile().getHeight() < y.getTile().getHeight()) {
                        return x;
                    }
                    return y;
                }).map(TileWatershedHolder::getWatershed);
    }

    @Getter
    private static class TileWatershedHolder {
        private final Tile tile;
        private final Watershed watershed;

        private TileWatershedHolder(Tile tile) {
            this.tile = tile;
            this.watershed = tile.getWatershed();
        }

        private boolean hasWatershed() {
            return this.watershed != null;
        }
    }

}
