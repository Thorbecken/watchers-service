package com.watchers.components.climate;

import com.watchers.model.climate.AircurrentType;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.SkyTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class PrecipiationComputator {

    static Map<AircurrentType, Function<WorldSettings, Integer>> airCurrentStrengthSetter = new HashMap<>();

    static {
        airCurrentStrengthSetter.put(AircurrentType.LATITUDAL, WorldSettings::getLatitudinalStrength);
        airCurrentStrengthSetter.put(AircurrentType.LONGITUDAL, WorldSettings::getLongitudinalStrength);
    }

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = taskDto.getWorld();
        List<Climate> climates = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .collect(Collectors.toList());

        computeEvaporation(climates);
        moveCloudsAccordingToAirflow(climates, world.getWorldSettings());
        computePrecipitation(climates);
    }

    private void computeEvaporation(List<Climate> climates) {
        climates.stream()
                .filter(Climate::isWater)
                .forEach(this::procesWaterClimate);
    }

    @Transactional
    private void procesWaterClimate(Climate climate) {
        climate.getSkyTile().addAirMoisture(10);
    }

    @Transactional
    protected void moveCloudsAccordingToAirflow(List<Climate> climates, WorldSettings worldSettings) {
        climates.stream()
                .map(Climate::getSkyTile)
                .flatMap(skyTile -> skyTile.getOutgoingAircurrents().stream())
                .forEach(aircurrent -> {
                    AircurrentType aircurrentType = aircurrent.getAircurrentType();
                    int currentStrength = airCurrentStrengthSetter.get(aircurrentType).apply(worldSettings);
                    aircurrent.setCurrentStrength(currentStrength);
                });

        climates.stream()
                .map(Climate::getSkyTile)
                .forEach(SkyTile::moveClouds);
        climates.stream()
                .map(Climate::getSkyTile)
                .forEach(SkyTile::processIncommingMoisture);
    }

    protected void computePrecipitation(List<Climate> climates) {
        climates.parallelStream()
                .filter(Climate::isLand)
                .forEach(this::procesLandClimate);
    }

    @Transactional
    private void procesLandClimate(Climate climate) {
        SkyTile currentSkyTile = climate.getSkyTile();
        double currentAirmoisture = currentSkyTile.getAirMoisture();
        // the 1 below is diurnal Rainfall
        double precipitation = currentAirmoisture / 10 + 1;
        currentSkyTile.setAirMoistureLossage(precipitation);
        currentSkyTile.calculateNewMoistureLevel();
    }
}