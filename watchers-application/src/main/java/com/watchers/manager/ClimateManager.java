package com.watchers.manager;

import com.watchers.components.climate.*;
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
        aircurrentRecalibrator.process(taskDto);
        temperatureZoneComputator.process(taskDto);
        precipiationComputator.process(taskDto);
        climateComputator.process(taskDto);
        biomeComputator.process(taskDto);
    }
}
