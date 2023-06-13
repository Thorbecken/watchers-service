package com.watchers.components.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
@AllArgsConstructor
public class WaterErosionComputator {

    @Transactional
    public void process(WorldTaskDto taskDto) {
        if (taskDto instanceof ContinentalDriftTaskDto) {
            World world = taskDto.getWorld();
            world.getCoordinates().stream()
                    .map(Coordinate::getTile)
                    .filter(Tile::isLand)
                    .sorted(Comparator.comparing(Tile::getHeight))
                    .filter(tile -> tile.getDownWardTile()!=null)
                    .forEach(tile -> {
                        Tile downwardTile = tile.getDownWardTile();
                        double numberOfErosionCounters = tile.getDownFlowAmount() / 10d;
                        for (double i = 0; i < numberOfErosionCounters; i++) {
                            long heightDifference = tile.getHeight() - downwardTile.getHeight();
                            if (heightDifference > 2) {
                                downwardTile.setHeight(downwardTile.getHeight() + 1);
                                tile.setHeight(tile.getHeight() - 1);
                            }
                        }
                    });
        }
    }
}
