package com.watchers.components.climate;

import com.watchers.model.climate.*;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class PrecipiationComputator {

    private final static long WET_ZONE = 60;
    private final static long HUMID_ZONE = 40;
    private final static long SEMI_ARID_ZONE = 20;
    private final static long ARID_ZONE = 0;

    private final static long WET_PRECIPITION = 10;
    private final static long HUMID_PRECIPITION = 5;
    private final static long SEMI_ARID__PRECIPITION = 2;
    private final static long ARID_PRECIPITION = 1;
    private final static long NO_PRECIPITION = 0;

    static Map<PrecipitationEnum, Long> precipationMap = new HashMap<>();

    static {
        precipationMap.put(PrecipitationEnum.WET, WET_PRECIPITION);
        precipationMap.put(PrecipitationEnum.HUMID, HUMID_PRECIPITION);
        precipationMap.put(PrecipitationEnum.SEMI_ARID, SEMI_ARID__PRECIPITION);
        precipationMap.put(PrecipitationEnum.ARID, ARID_ZONE);
    }

    private WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = worldRepository.getOne(taskDto.getWorldId());
        List<Climate> climates = world.getCoordinates().stream().map(Coordinate::getClimate).collect(Collectors.toList());

        computeEvaporation(climates);
        moveCloudsAccordingToAirflow(climates);
        setLandPrecipationEnums(climates);

        worldRepository.save(world);
    }

    private void computeEvaporation(List<Climate> climates) {
        climates.parallelStream()
                .filter(Climate::isWater)
                .forEach(this::procesWaterClimate);
    }

    @Transactional
    private void procesWaterClimate(Climate climate) {
        climate.getSkyTile().addAirMoisture(10);
        climate.setPrecipitationEnum(PrecipitationEnum.WET);
    }

    protected void moveCloudsAccordingToAirflow(List<Climate> climates) {
        climates.parallelStream()
                .map(Climate::getSkyTile)
                .forEach(SkyTile::moveClouds);
        climates.parallelStream()
                .map(Climate::getSkyTile)
                .forEach(SkyTile::processIncommingMoisture);
    }

    protected void setLandPrecipationEnums(List<Climate> climates) {
        climates.parallelStream()
                .filter(Climate::isLand)
                .forEach(this::procesLandClimate);
    }

    private void procesLandClimate(Climate climate){
            SkyTile currentSkyTile = climate.getSkyTile();
            if (currentSkyTile.getAirMoisture() >= WET_ZONE) {
                currentSkyTile.setAirMoistureLossage(WET_PRECIPITION);
            } else if (currentSkyTile.getAirMoisture() >= HUMID_ZONE) {
                currentSkyTile.setAirMoistureLossage(HUMID_PRECIPITION);
            } else if (currentSkyTile.getAirMoisture() >= SEMI_ARID_ZONE) {
                currentSkyTile.setAirMoistureLossage(SEMI_ARID__PRECIPITION);
            } else if (currentSkyTile.getAirMoisture() >= ARID_ZONE) {
                currentSkyTile.setAirMoistureLossage(ARID_PRECIPITION);
            } else {
                currentSkyTile.setAirMoistureLossage(NO_PRECIPITION);
            }

            currentSkyTile.calculateNewMoistureLevel();
            climate.setPrecipitationEnum(precipationCaluculator(currentSkyTile.getAirMoistureLossage()));
    }

    private PrecipitationEnum precipationCaluculator(double airMoistureLossage) {
        if (airMoistureLossage >= WET_PRECIPITION) {
            return PrecipitationEnum.WET;
        } else if (airMoistureLossage >= HUMID_PRECIPITION) {
            return PrecipitationEnum.HUMID;
        } else if (airMoistureLossage >= SEMI_ARID__PRECIPITION) {
            return PrecipitationEnum.SEMI_ARID;
        } else if (airMoistureLossage >= ARID_PRECIPITION) {
            return PrecipitationEnum.ARID;
        } else {
            return PrecipitationEnum.ARID;
        }
    }
}