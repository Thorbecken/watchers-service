package com.watchers.components.climate;

import com.watchers.model.climate.AirCurrent;
import com.watchers.model.climate.Cloud;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.climate.Climate;
import com.watchers.model.climate.PrecipitationEnum;
import com.watchers.model.world.World;
import com.watchers.repository.inmemory.WorldRepositoryInMemory;
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

    private final static int HUMID_ZONE = 12;
    private final static int SEMI_ARID_ZONE = 4;
    private final static int ARID_ZONE = 0;

    private final static int HUMID_PRECIPITION = 4;
    private final static int SEMI_ARID__PRECIPITION = 2;
    private final static int ARID__PRECIPITION = 1;

    private WorldRepositoryInMemory worldRepositoryInMemory;

    @Transactional("inmemoryDatabaseTransactionManager")
    public void process(WorldTaskDto taskDto) {
        World world = worldRepositoryInMemory.getOne(taskDto.getWorldId());
        List<Climate> climates = world.getCoordinates().stream().map(Coordinate::getClimate).collect(Collectors.toList());

        computeEvaporation(climates);
        moveCloudsAccordingToAirflow(world.getCoordinates());
        setLandPrecipationEnums(climates);

        worldRepositoryInMemory.save(world);
    }

    private void computeEvaporation(List<Climate> climates) {
        climates.parallelStream()
                .filter(Climate::isWater)
                .forEach(climate -> {
                    climate.getCurrentCloud().addAirMoisture(3);
                    climate.setPrecipitationEnum(PrecipitationEnum.ARID);
                });
    }

    protected void moveCloudsAccordingToAirflow(Set<Coordinate> coordinates) {
        //coordinates.forEach(coordinate -> log(coordinate.getClimate(), "beforeAirflow"));
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
        //coordinates.forEach(coordinate -> log(coordinate.getClimate(), "afterAirflow"));
    }

    protected void setLandPrecipationEnums(List<Climate> climates) {
        climates.parallelStream()
                .filter(Climate::isLand)
                .forEach(climate -> {
                            Cloud currentCloud = climate.getCurrentCloud();
                            if (currentCloud.getAirMoisture() >= HUMID_ZONE) {
                                climate.setPrecipitationEnum(PrecipitationEnum.WET);
                                currentCloud.reduceAirMoisture(HUMID_PRECIPITION);
                            } else if (currentCloud.getAirMoisture() > HUMID_ZONE) {
                                climate.setPrecipitationEnum(PrecipitationEnum.HUMID);
                                currentCloud.reduceAirMoisture(HUMID_PRECIPITION);
                            } else if (currentCloud.getAirMoisture() > SEMI_ARID_ZONE) {
                                climate.setPrecipitationEnum(PrecipitationEnum.SEMI_ARID);
                                currentCloud.reduceAirMoisture(SEMI_ARID__PRECIPITION);
                            } else if (currentCloud.getAirMoisture() > ARID_ZONE) {
                                climate.setPrecipitationEnum(PrecipitationEnum.ARID);
                                currentCloud.reduceAirMoisture(ARID__PRECIPITION);
                            } else {
                                climate.setPrecipitationEnum(PrecipitationEnum.ARID);
                                currentCloud.reduceAirMoisture(ARID__PRECIPITION);
                            }
                        }
                );
    }
}