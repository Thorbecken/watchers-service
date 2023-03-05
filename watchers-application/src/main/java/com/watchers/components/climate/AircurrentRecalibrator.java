package com.watchers.components.climate;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.Climate;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AircurrentRecalibrator {

    private final WorldRepository worldRepository;

    @Transactional
    // Hibernate can't handle that this method uses parallel streams
    public void process(WorldTaskDto taskDto) {
        if (taskDto instanceof ContinentalDriftTaskDto) {
            World world = worldRepository.getById(taskDto.getWorldId());
            world.getCoordinates().stream()
                    .map(Coordinate::getClimate)
                    .map(Climate::getSkyTile)
                    .flatMap(skyTile -> skyTile.getIncommingAircurrents().stream())
                    .forEach(Aircurrent::recalculateHeigthDifference);

            worldRepository.saveAndFlush(world);
        }
    }
}
