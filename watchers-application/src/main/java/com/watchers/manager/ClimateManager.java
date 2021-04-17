package com.watchers.manager;

import com.watchers.components.climate.*;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.dto.WorldTaskDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClimateManager {

    private TemperatureZoneComputator temperatureZoneComputator;
    private PrecipiationComputator precipiationComputator;
    private BiomeComputator biomeComputator;
    private ClimateComputator climateComputator;
    private AircurrentRecalibrator aircurrentRecalibrator;

    public void proces(WorldTaskDto taskDto){
        StopwatchTimer.start();
        aircurrentRecalibrator.process(taskDto);
        StopwatchTimer.stop("aircurrentRecalibrator");
        StopwatchTimer.start();
        temperatureZoneComputator.process(taskDto);
        StopwatchTimer.stop("temperatureZoneComputator");
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
