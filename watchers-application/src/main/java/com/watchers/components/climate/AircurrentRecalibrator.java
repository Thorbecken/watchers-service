package com.watchers.components.climate;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AircurrentRecalibrator {

    @Transactional
    // Hibernate can't handle that this method uses parallel streams
    public void process(WorldTaskDto taskDto) {
        if (taskDto instanceof ContinentalDriftTaskDto) {
            World world = taskDto.getWorld();
            world.getCoordinates().stream()
                    .map(Coordinate::getClimate)
                    .flatMap(skyTile -> skyTile.getIncomingAircurrents().stream())
                    .forEach(Aircurrent::recalculateHeigthDifference);
        }
    }
}
