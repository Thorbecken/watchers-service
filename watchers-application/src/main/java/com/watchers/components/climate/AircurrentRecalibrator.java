package com.watchers.components.climate;

import com.watchers.model.climate.Aircurrent;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AircurrentRecalibrator {

    private final WorldRepository worldRepository;

    @Transactional
    // Hibernate can't handle that this method uses parallel streams
    public void process(WorldTaskDto taskDto) {
        if (taskDto instanceof ContinentalDriftTaskDto) {
            World world = worldRepository.getOne(taskDto.getWorldId());
            Set<Coordinate> coordinateList = world.getCoordinates();
            List<Climate> climateList = coordinateList.stream()
                    .map(Coordinate::getClimate)
                    .collect(Collectors.toList());
            List<SkyTile> skyTileList = climateList.stream()
                    .map(Climate::getSkyTile)
                    .collect(Collectors.toList());
            List<Aircurrent> aircurrentList = skyTileList.stream()
                    .flatMap(skyTile -> skyTile.getIncommingAircurrents().stream())
                    .collect(Collectors.toList());

            aircurrentList
                    .forEach(Aircurrent::recalculateHeigthDifference);
            worldRepository.save(world);
        }
    }
}
