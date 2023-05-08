package com.watchers.components.climate;

import com.watchers.model.climate.Climate;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
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
    public void process(WorldTaskDto taskDto) {
        World world = taskDto.getWorld();

        restoreBaseTemperature(world);
        for (int i = 0; i < 3; i++) {
            // waterflow transfer
            transferWaterTemperature(world);
            // airflow transfer
            transferAirTemperature(world);
            // proces transfer
            processTemperatureTransfer(world);
        }
    }

    private void restoreBaseTemperature(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .forEach(Climate::restoreBaseTemperature);
    }

    private void processTemperatureTransfer(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .forEach(Climate::processHeatChange);
    }

    private void transferWaterTemperature(World world) {
        world.getCoordinates().stream()
                .filter(Coordinate::isWater)
                .map(Coordinate::getClimate)
                .forEach(Climate::transferWaterTemperature);
    }

    private void transferAirTemperature(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .forEach(Climate::transferAirTemperature);
    }
}
