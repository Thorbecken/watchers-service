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

    private WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto){
        World world = worldRepository.getOne(taskDto.getWorldId());
        world.getCoordinates()
                .parallelStream()
                .map(Coordinate::getClimate)
                .forEach(climate -> climate.setTemperatureEnum(calculateTemperatureEnum(climate)));

        worldRepository.save(world);
    }

    private TemperatureEnum calculateTemperatureEnum(Climate climate) {
        if(climate.getLatitude() <= -60){
            return TemperatureEnum.POLAR;
        } else if(climate.getLatitude() <= -30){
            return TemperatureEnum.TEMPERATE;
        } else if(climate.getLatitude() <= 30){
            return TemperatureEnum.TROPICAL;
        } else if(climate.getLatitude() <= 60){
            return TemperatureEnum.TEMPERATE;
        } else {
            return TemperatureEnum.POLAR;
        }
    }

}
