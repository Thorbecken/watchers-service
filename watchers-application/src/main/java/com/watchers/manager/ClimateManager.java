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

    private final TemperatureZoneComputator temperatureZoneComputator;
    private final PrecipiationComputator precipiationComputator;
    private final AircurrentRecalibrator aircurrentRecalibrator;
    private final WaterflowComputator waterflowComputator;
    private final WaterErosionComputator waterErosionComputator;

    public void proces(WorldTaskDto taskDto){
        if(taskDto instanceof ContinentalDriftTaskDto) {
            StopwatchTimer.start();
            aircurrentRecalibrator.process(taskDto);
            StopwatchTimer.stop("aircurrentRecalibrator");

            StopwatchTimer.start();
            temperatureZoneComputator.process(taskDto);
            StopwatchTimer.stop("temperatureZoneComputator");
        }
        StopwatchTimer.start();
        precipiationComputator.process(taskDto);
        StopwatchTimer.stop("precipiationComputator");
        StopwatchTimer.start();
        waterflowComputator.process(taskDto);
        StopwatchTimer.stop("waterflowComputator");
        if(taskDto instanceof ContinentalDriftTaskDto) {
            StopwatchTimer.start();
            waterErosionComputator.process(taskDto);
            StopwatchTimer.stop("waterErosionComputator");
        }
    }
}
