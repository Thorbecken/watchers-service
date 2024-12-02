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

        adjustTemperaturesForCurrentAltitudesAndSeaLevel(world);
        for (int i = 0; i < 3; i++) {
            // waterflow transfer
            calculateTemperatureTransferForLargeBodiesOfWater(world);
            // airflow transfer
            calculateTemperatureTransferByAir(world);
            // proces transfer
            processTemperatureTransfers(world);
        }

        recalculateMaximalAirMoisture(world);
    }

    private void adjustTemperaturesForCurrentAltitudesAndSeaLevel(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .forEach(climate -> climate.calculateAdjustedTemperatureForAltitude(world.getSeaLevel()));
    }

    private void processTemperatureTransfers(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .forEach(Climate::processHeatChange);
    }

    private void calculateTemperatureTransferForLargeBodiesOfWater(World world) {
        world.getCoordinates().stream()
                .filter(Coordinate::isWater)
                .map(Coordinate::getClimate)
                .forEach(Climate::transferWaterTemperature);
    }

    private void calculateTemperatureTransferByAir(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .forEach(Climate::transferAirTemperature);
    }

    private void recalculateMaximalAirMoisture(World world) {
        world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .forEach(Climate::calculateNewMoistureLevel);
    }
}
