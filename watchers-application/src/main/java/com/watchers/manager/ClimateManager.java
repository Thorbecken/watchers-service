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
    private final WatershedComputator watershedComputator;
    private final RiverComputator riverComputator;

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
        watershedComputator.process(taskDto);
        StopwatchTimer.stop("watershedComputator");
        StopwatchTimer.start();
        riverComputator.process(taskDto);
        StopwatchTimer.stop("riverComputator");
        StopwatchTimer.start();
        climateComputator.process(taskDto);
        StopwatchTimer.stop("climateComputator");
        StopwatchTimer.start();
        biomeComputator.process(taskDto);
        StopwatchTimer.stop("biomeComputator");
    }
}
