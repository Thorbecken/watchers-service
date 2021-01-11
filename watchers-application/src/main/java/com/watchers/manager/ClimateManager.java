package com.watchers.manager;

import com.watchers.components.climate.BiomeComputator;
import com.watchers.components.climate.ClimateComputator;
import com.watchers.components.climate.PrecipiationComputator;
import com.watchers.components.climate.TemperatureZoneComputator;
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

    public void proces(WorldTaskDto taskDto){
        temperatureZoneComputator.process(taskDto);
        precipiationComputator.process(taskDto);
        climateComputator.process(taskDto);
        biomeComputator.process(taskDto);
    }
}
