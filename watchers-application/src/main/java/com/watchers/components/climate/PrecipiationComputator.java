package com.watchers.components.climate;

import com.watchers.model.climate.AirCurrent;
import com.watchers.model.climate.Cloud;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.PrecipitationEnum;
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
    private final static long HUMID_ZONE = 30;
    private final static long SEMI_ARID_ZONE = 15;
    private final static long ARID_ZONE = 0;

    private final static long WET_PRECIPITION = 10;
    private final static long HUMID_PRECIPITION = 5;
    private final static long SEMI_ARID__PRECIPITION = 3;
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
        moveCloudsAccordingToAirflow(world.getCoordinates());
        setLandPrecipationEnums(climates);

        worldRepository.save(world);
    }

    private void computeEvaporation(List<Climate> climates) {
        climates.parallelStream()
                .filter(Climate::isWater)
                .forEach(this::procesWaterClimate);
    }

    private void procesWaterClimate(Climate climate) {
        climate.getCurrentCloud().addAirMoisture(10);
        climate.setPrecipitationEnum(PrecipitationEnum.WET);
    }

    protected void moveCloudsAccordingToAirflow(Set<Coordinate> coordinates) {
        Map<Long, List<Coordinate>> airCurrentsMap = coordinates.stream()
                .collect(Collectors.groupingBy(Coordinate::getYCoord));
        Map<Long, AirCurrent> airCurrents = airCurrentsMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new AirCurrent(entry.getValue().stream()
                        .map(Coordinate::getClimate)
                        .collect(Collectors.toList()))));
        Set<AirCurrent> currents = airCurrents.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        currents.forEach(AirCurrent::moveClouds);
    }

    protected void setLandPrecipationEnums(List<Climate> climates) {
        climates.parallelStream()
                .filter(Climate::isLand)
                .forEach(this::procesLandClimate);
    }

    private void procesLandClimate(Climate climate){
            Cloud currentCloud = climate.getCurrentCloud();
            if (currentCloud.getAirMoisture() >= WET_ZONE) {
                currentCloud.setAirMoistureLossage(WET_PRECIPITION);
            } else if (currentCloud.getAirMoisture() >= HUMID_ZONE) {
                currentCloud.setAirMoistureLossage(HUMID_PRECIPITION);
            } else if (currentCloud.getAirMoisture() >= SEMI_ARID_ZONE) {
                currentCloud.setAirMoistureLossage(SEMI_ARID__PRECIPITION);
            } else if (currentCloud.getAirMoisture() >= ARID_ZONE) {
                currentCloud.setAirMoistureLossage(ARID_PRECIPITION);
            } else {
                currentCloud.setAirMoistureLossage(NO_PRECIPITION);
            }

            currentCloud.calculateHeightDifferenceEffect();
            currentCloud.calculateNewMoistureLevel();
            climate.setPrecipitationEnum(precipationCaluculator(currentCloud.getAirMoistureLossage()));
            //climate.getCurrentCloud().reduceAirMoisture(precipationMap.get(climate.getPrecipitationEnum()));
    }

    private PrecipitationEnum precipationCaluculator(long airMoistureLossage) {
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