package com.watchers.components.climate;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.climate.ClimateEnum;
import com.watchers.model.climate.PrecipitationEnum;
import com.watchers.model.climate.TemperatureEnum;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class ClimateComputator {

    private WorldRepository worldRepository;

    private static final Map<TemperatureEnum, Map<PrecipitationEnum ,ClimateEnum>> climatemap;
    static {
        Map<PrecipitationEnum, ClimateEnum> polarMap = new HashMap<>();
        polarMap.put(PrecipitationEnum.ARID, ClimateEnum.POLAR_ARID);
        polarMap.put(PrecipitationEnum.SEMI_ARID, ClimateEnum.POLAR_SEMI_ARID);
        polarMap.put(PrecipitationEnum.HUMID, ClimateEnum.POLAR_HUMID);
        polarMap.put(PrecipitationEnum.WET, ClimateEnum.POLAR_WET);

        Map<PrecipitationEnum, ClimateEnum> temperateMap = new HashMap<>();
        temperateMap.put(PrecipitationEnum.ARID, ClimateEnum.TEMPERATE_ARID);
        temperateMap.put(PrecipitationEnum.SEMI_ARID, ClimateEnum.TEMPERATE_SEMI_ARID);
        temperateMap.put(PrecipitationEnum.HUMID, ClimateEnum.TEMPERATE_HUMID);
        temperateMap.put(PrecipitationEnum.WET, ClimateEnum.TEMPERATE_WET);

        Map<PrecipitationEnum, ClimateEnum> tropicalMap = new HashMap<>();
        tropicalMap.put(PrecipitationEnum.ARID, ClimateEnum.TROPICAL_ARID);
        tropicalMap.put(PrecipitationEnum.SEMI_ARID, ClimateEnum.TROPICAL_SEMI_ARID);
        tropicalMap.put(PrecipitationEnum.HUMID, ClimateEnum.TROPICAL_HUMID);
        tropicalMap.put(PrecipitationEnum.WET, ClimateEnum.TROPICAL_WET);

        climatemap = new HashMap<>();
        climatemap.put(TemperatureEnum.POLAR, polarMap);
        climatemap.put(TemperatureEnum.TEMPERATE, temperateMap);
        climatemap.put(TemperatureEnum.TROPICAL, tropicalMap);
    }

    @Transactional
    public void process(WorldTaskDto taskDto){
        World world = worldRepository.getOne(taskDto.getWorldId());
        world.getCoordinates()
                .parallelStream()
                .map(Coordinate::getClimate)
                .forEach(climate -> climate.setClimateEnum(climatemap.get(climate.getTemperatureEnum()).get(climate.getPrecipitationEnum())));

        worldRepository.save(world);
    }
}
