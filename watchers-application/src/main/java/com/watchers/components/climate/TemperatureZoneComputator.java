package com.watchers.components.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.TemperatureEnum;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class TemperatureZoneComputator {

    private final WorldRepository worldRepository;

    @Transactional
    public void processWithoutLoadingAndSaving(WorldTaskDto taskDto) {
        World world = worldRepository.getById(taskDto.getWorldId());
        this.process(world);
        worldRepository.save(world);
    }

    @Transactional
    public void processWithoutLoadingAndSaving(World world) {
        this.process(world);
    }

    private void process(World world) {
        world.getCoordinates()
                .parallelStream()
                .map(Coordinate::getClimate)
                .forEach(climate -> climate.setTemperatureEnum(calculateTemperatureEnum(climate)));
    }

    private TemperatureEnum calculateTemperatureEnum(Climate climate) {
        if (climate.getLatitude() <= -60) {
            return TemperatureEnum.POLAR;
        } else if (climate.getLatitude() <= -30) {
            return TemperatureEnum.TEMPERATE;
        } else if (climate.getLatitude() <= 30) {
            return TemperatureEnum.TROPICAL;
        } else if (climate.getLatitude() <= 60) {
            return TemperatureEnum.TEMPERATE;
        } else {
            return TemperatureEnum.POLAR;
        }
    }

}
