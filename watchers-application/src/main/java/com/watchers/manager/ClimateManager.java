package com.watchers.manager;

import com.watchers.components.climate.*;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClimateManager {

    private final TemperatureZoneComputer temperatureZoneComputator;
    private final PrecipiationComputer precipiationComputer;
    private final WaterflowComputer waterflowComputer;
    private final WaterErosionComputer waterErosionComputer;

    public void process(WorldTaskDto taskDto){
        if(taskDto instanceof ContinentalDriftTaskDto) {
            StopwatchTimer.processAndTime(temperatureZoneComputator, taskDto);
        }
        StopwatchTimer.processAndTime(precipiationComputer, taskDto);
        StopwatchTimer.processAndTime(waterflowComputer, taskDto);
        if(taskDto instanceof ContinentalDriftTaskDto) {
            StopwatchTimer.processAndTime(waterErosionComputer, taskDto);
        }
    }
}
