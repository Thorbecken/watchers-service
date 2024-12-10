package com.watchers.components.climate;

import com.watchers.model.climate.Climate;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TemperatureZoneComputator {

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = taskDto.getWorld();
        Climate.recalculateTemperatures(world);
    }
}
