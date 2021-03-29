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

    private WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto){
        if(taskDto instanceof ContinentalDriftTaskDto) {
            World world = worldRepository.getOne(taskDto.getWorldId());
            world.getCoordinates()
                    .parallelStream()
                    .map(Coordinate::getClimate)
                    .map(Climate::getSkyTile)
                    .flatMap(skyTile -> skyTile.getIncommingAircurrents().stream())
                    .forEach(Aircurrent::recalculateHeigthDifference);

            worldRepository.save(world);
        }
    }
}
