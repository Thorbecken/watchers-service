package com.watchers.components.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
public class WaterErosionComputator {

    public WaterErosionComputator(
            @Value("${watch.continent.water.erosion.strength}") int erosionStrength){
        this.erosionStrength = erosionStrength;
    }

    private final int erosionStrength;


    @Transactional
    public void process(WorldTaskDto taskDto) {
        if (taskDto instanceof ContinentalDriftTaskDto) {
            World world = taskDto.getWorld();

            for (int i = 0; i < erosionStrength; i++) {
                process(world);
            }
        }
    }

    protected void process(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile.getDownWardTile() != null
                        && tile.getHeight() > tile.getDownWardTile().getHeight())
                .sorted(Comparator.comparing(Tile::getHeight))
                .forEach(this::erodeTile);
    }

    protected void erodeTile(Tile tile){
        Tile downwardTile = tile.getDownWardTile();
        double halfWayErosionPoint = (double) ((tile.getHeight() - downwardTile.getHeight()) / 2);
        double maxErosion = Math.max(0, halfWayErosionPoint);
        double waterErosion = Math.min(maxErosion, tile.getSurfaceWater());

        downwardTile.setHeight((long) (downwardTile.getHeight() + waterErosion));
        tile.setHeight((long) (tile.getHeight() - waterErosion));
    }

}
