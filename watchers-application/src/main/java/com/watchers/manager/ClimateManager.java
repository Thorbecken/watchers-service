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
    private final BiomeComputator biomeComputator;
    private final ClimateComputator climateComputator;
    private final AircurrentRecalibrator aircurrentRecalibrator;

    public void proces(WorldTaskDto taskDto){
        if(taskDto instanceof ContinentalDriftTaskDto) {
            StopwatchTimer.start();
            aircurrentRecalibrator.process(taskDto);
            StopwatchTimer.stop("aircurrentRecalibrator");

            StopwatchTimer.start();
            temperatureZoneComputator.processWithoutLoadingAndSaving(taskDto);
            StopwatchTimer.stop("temperatureZoneComputator");
        }
        StopwatchTimer.start();
        precipiationComputator.process(taskDto);
        StopwatchTimer.stop("precipiationComputator");
        StopwatchTimer.start();
        climateComputator.process(taskDto);
        StopwatchTimer.stop("climateComputator");
        StopwatchTimer.start();
        biomeComputator.process(taskDto);
        StopwatchTimer.stop("biomeComputator");
    }
}
