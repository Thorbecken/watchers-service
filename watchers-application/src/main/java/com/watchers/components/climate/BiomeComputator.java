package com.watchers.components.climate;

import com.watchers.model.climate.ClimateEnum;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.*;
import com.watchers.model.world.World;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class BiomeComputator {

    private WorldRepository worldRepository;

    private static final Map<SurfaceType, Map<ClimateEnum, BiomeTypeEnum>> biomeTypes;
    static {
        biomeTypes = new HashMap<>();

        Map<ClimateEnum, BiomeTypeEnum> oceanMap = new HashMap<>();
        oceanMap.put(ClimateEnum.OCEAN, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.POLAR_ARID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.POLAR_SEMI_ARID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.POLAR_HUMID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.POLAR_WET, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TEMPERATE_ARID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TEMPERATE_SEMI_ARID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TEMPERATE_HUMID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TEMPERATE_WET, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TROPICAL_ARID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TROPICAL_SEMI_ARID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TROPICAL_HUMID, BiomeTypeEnum.OCEAN);
        oceanMap.put(ClimateEnum.TROPICAL_WET, BiomeTypeEnum.OCEAN);

        Map<ClimateEnum, BiomeTypeEnum> seaMap = new HashMap<>();
        seaMap.put(ClimateEnum.SEA, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.POLAR_ARID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.POLAR_SEMI_ARID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.POLAR_HUMID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.POLAR_WET, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TEMPERATE_ARID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TEMPERATE_SEMI_ARID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TEMPERATE_HUMID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TEMPERATE_WET, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TROPICAL_ARID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TROPICAL_SEMI_ARID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TROPICAL_HUMID, BiomeTypeEnum.SEA);
        seaMap.put(ClimateEnum.TROPICAL_WET, BiomeTypeEnum.SEA);

        Map<ClimateEnum, BiomeTypeEnum> coastalMap = new HashMap<>();
        coastalMap.put(ClimateEnum.COASTAL, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.POLAR_ARID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.POLAR_SEMI_ARID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.POLAR_HUMID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.POLAR_WET, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TEMPERATE_ARID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TEMPERATE_SEMI_ARID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TEMPERATE_HUMID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TEMPERATE_WET, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TROPICAL_ARID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TROPICAL_SEMI_ARID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TROPICAL_HUMID, BiomeTypeEnum.COASTAL);
        coastalMap.put(ClimateEnum.TROPICAL_WET, BiomeTypeEnum.COASTAL);

        Map<ClimateEnum, BiomeTypeEnum> plainMap = new HashMap<>();
        plainMap.put(ClimateEnum.POLAR_ARID, BiomeTypeEnum.POLAR_PLAIN_DESERT);
        plainMap.put(ClimateEnum.POLAR_SEMI_ARID, BiomeTypeEnum.POLAR_PLAIN_TUNDRA);
        plainMap.put(ClimateEnum.POLAR_HUMID, BiomeTypeEnum.POLAR_PLAIN_TAIGA);
        plainMap.put(ClimateEnum.POLAR_WET, BiomeTypeEnum.POLAR_PLAIN_TAIGA);
        plainMap.put(ClimateEnum.TEMPERATE_ARID, BiomeTypeEnum.TEMPERATE_PLAIN_DESERT);
        plainMap.put(ClimateEnum.TEMPERATE_SEMI_ARID, BiomeTypeEnum.TEMPERATE_PLAIN_GRASSLAND);
        plainMap.put(ClimateEnum.TEMPERATE_HUMID, BiomeTypeEnum.TEMPERATE_PLAIN_FOREST);
        plainMap.put(ClimateEnum.TEMPERATE_WET, BiomeTypeEnum.TEMPERATE_PLAIN_FOREST);
        plainMap.put(ClimateEnum.TROPICAL_ARID, BiomeTypeEnum.TROPICAL_PLAIN_DESERT);
        plainMap.put(ClimateEnum.TROPICAL_SEMI_ARID, BiomeTypeEnum.TROPICAL_PLAIN_SAVANAH);
        plainMap.put(ClimateEnum.TROPICAL_HUMID, BiomeTypeEnum.TROPICAL_PLAIN_JUNGLE);
        plainMap.put(ClimateEnum.TROPICAL_WET, BiomeTypeEnum.TROPICAL_PLAIN_JUNGLE);

        Map<ClimateEnum, BiomeTypeEnum> hillMap = new HashMap<>();
        hillMap.put(ClimateEnum.POLAR_ARID, BiomeTypeEnum.POLAR_HILL_DESERT);
        hillMap.put(ClimateEnum.POLAR_SEMI_ARID, BiomeTypeEnum.POLAR_HILL_TUNDRA);
        hillMap.put(ClimateEnum.POLAR_HUMID, BiomeTypeEnum.POLAR_HILL_TAIGA);
        hillMap.put(ClimateEnum.POLAR_WET, BiomeTypeEnum.POLAR_HILL_TAIGA);
        hillMap.put(ClimateEnum.TEMPERATE_ARID, BiomeTypeEnum.TEMPERATE_HILL_DESERT);
        hillMap.put(ClimateEnum.TEMPERATE_SEMI_ARID, BiomeTypeEnum.TEMPERATE_HILL_GRASSLAND);
        hillMap.put(ClimateEnum.TEMPERATE_HUMID, BiomeTypeEnum.TEMPERATE_HILL_FOREST);
        hillMap.put(ClimateEnum.TEMPERATE_WET, BiomeTypeEnum.TEMPERATE_HILL_FOREST);
        hillMap.put(ClimateEnum.TROPICAL_ARID, BiomeTypeEnum.TROPICAL_HILL_DESERT);
        hillMap.put(ClimateEnum.TROPICAL_SEMI_ARID, BiomeTypeEnum.TROPICAL_HILL_SAVANAH);
        hillMap.put(ClimateEnum.TROPICAL_HUMID, BiomeTypeEnum.TROPICAL_HILL_JUNGLE);
        hillMap.put(ClimateEnum.TROPICAL_WET, BiomeTypeEnum.TROPICAL_HILL_JUNGLE);

        Map<ClimateEnum, BiomeTypeEnum> mountainMap = new HashMap<>();
        mountainMap.put(ClimateEnum.POLAR_ARID, BiomeTypeEnum.POLAR_MOUNTAIN_DESERT);
        mountainMap.put(ClimateEnum.POLAR_SEMI_ARID, BiomeTypeEnum.POLAR_MOUNTAIN_TUNDRA);
        mountainMap.put(ClimateEnum.POLAR_HUMID, BiomeTypeEnum.POLAR_MOUNTAIN_TAIGA);
        mountainMap.put(ClimateEnum.POLAR_WET, BiomeTypeEnum.POLAR_MOUNTAIN_TAIGA);
        mountainMap.put(ClimateEnum.TEMPERATE_ARID, BiomeTypeEnum.TEMPERATAE_MOUNTAIN_DESERT);
        mountainMap.put(ClimateEnum.TEMPERATE_SEMI_ARID, BiomeTypeEnum.TEMPERATE_MOUNTAIN_GRASSLAND);
        mountainMap.put(ClimateEnum.TEMPERATE_HUMID, BiomeTypeEnum.TEMPERATE_MOUNTAIN_FOREST);
        mountainMap.put(ClimateEnum.TEMPERATE_WET, BiomeTypeEnum.TEMPERATE_MOUNTAIN_FOREST);
        mountainMap.put(ClimateEnum.TROPICAL_ARID, BiomeTypeEnum.TROPICAL_MOUNTAIN_DESERT);
        mountainMap.put(ClimateEnum.TROPICAL_SEMI_ARID, BiomeTypeEnum.TROPICAL_MOUNTAIN_SAVANAH);
        mountainMap.put(ClimateEnum.TROPICAL_HUMID, BiomeTypeEnum.TROPICAL_MOUNTAIN_JUNGLE);
        mountainMap.put(ClimateEnum.TROPICAL_WET, BiomeTypeEnum.TROPICAL_MOUNTAIN_JUNGLE);

        biomeTypes.put(SurfaceType.OCEAN, oceanMap);
        biomeTypes.put(SurfaceType.SEA, seaMap);
        biomeTypes.put(SurfaceType.COASTAL, coastalMap);
        biomeTypes.put(SurfaceType.PLAIN, plainMap);
        biomeTypes.put(SurfaceType.HILL, hillMap);
        biomeTypes.put(SurfaceType.MOUNTAIN, mountainMap);
    }

    @Transactional
    public void process(WorldTaskDto taskDto){
        World world = worldRepository.getOne(taskDto.getWorldId());
        world.getCoordinates()
                .parallelStream()
                .forEach(
                        coordinate -> {
                            SurfaceType surfaceType = coordinate.getTile().getSurfaceType();
                            ClimateEnum climateEnum = coordinate.getClimate().getClimateEnum();
                            coordinate.getTile()
                                    .getBiome()
                                    .setBiomeTypeEnum(biomeTypes.get(surfaceType).get(climateEnum));
                        });

        worldRepository.save(world);
    }

}
