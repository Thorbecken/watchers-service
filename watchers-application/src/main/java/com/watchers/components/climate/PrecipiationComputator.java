package com.watchers.components.climate;

import com.watchers.model.climate.*;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import com.watchers.repository.WorldRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class PrecipiationComputator {

    static Map<PrecipitationEnum, Function<WorldSettings, Long>> precipationMap = new HashMap<>();
    static Map<AircurrentType, Function<WorldSettings, Integer>> airCurrentStrengthSetter = new HashMap<>();

    static {
        airCurrentStrengthSetter.put(AircurrentType.LATITUDAL, WorldSettings::getLatitudinalStrength);
        airCurrentStrengthSetter.put(AircurrentType.LONGITUDAL, WorldSettings::getLongitudinalStrength);

        precipationMap.put(PrecipitationEnum.WET, WorldSettings::getWetPrecipitation);
        precipationMap.put(PrecipitationEnum.HUMID, WorldSettings::getHumidPrecipitation);
        precipationMap.put(PrecipitationEnum.SEMI_ARID, WorldSettings::getSemiAridPrecipitation);
        precipationMap.put(PrecipitationEnum.ARID, WorldSettings::getAridPrecipitation);
    }

    private final WorldRepository worldRepository;

    @Transactional
    public void process(WorldTaskDto taskDto) {
        World world = worldRepository.getOne(taskDto.getWorldId());
        List<Climate> climates = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .collect(Collectors.toList());

        computeEvaporation(climates);
        moveCloudsAccordingToAirflow(climates, world.getWorldSettings());
        setLandPrecipationEnums(climates, world.getWorldSettings());

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

    @Transactional
    protected void moveCloudsAccordingToAirflow(List<Climate> climates, WorldSettings worldSettings) {
        List<SkyTile> skyTiles = climates.parallelStream()
                .map(Climate::getSkyTile)
                .collect(Collectors.toList());

        List<Aircurrent> aircurrents = skyTiles.stream()
                .flatMap(skyTile -> skyTile.getOutgoingAircurrents().stream())
                .collect(Collectors.toList());

        aircurrents.forEach(aircurrent -> {
            AircurrentType aircurrentType = aircurrent.getAircurrentType();
            int currentStrength = airCurrentStrengthSetter.get(aircurrentType).apply(worldSettings);
            aircurrent.setCurrentStrength(currentStrength);
        });

        climates.parallelStream()
                .map(Climate::getSkyTile)
                .forEach(SkyTile::moveClouds);
        climates.parallelStream()
                .map(Climate::getSkyTile)
                .forEach(SkyTile::processIncommingMoisture);
    }

    protected void setLandPrecipationEnums(List<Climate> climates, WorldSettings worldSettings) {
        LandClimateProcessor landClimateProcessor = new LandClimateProcessor(worldSettings);
        climates.parallelStream()
                .filter(Climate::isLand)
                .forEach(landClimateProcessor::procesLandClimate);
    }

    private class LandClimateProcessor {
        private final long WET_ZONE;
        private final long HUMID_ZONE;
        private final long SEMI_ARID_ZONE;
        private final long ARID_ZONE;

        private final long WET_PRECIPITION;
        private final long HUMID_PRECIPITION;
        private final long SEMI_ARID__PRECIPITION;
        private final long ARID_PRECIPITION;
        private static final long NO_PRECIPITION = 0;

        LandClimateProcessor(WorldSettings worldSettings) {
            this.WET_ZONE = worldSettings.getWetZone();
            this.HUMID_ZONE = worldSettings.getHumidZone();
            this.SEMI_ARID_ZONE = worldSettings.getSemiAridZone();
            this.ARID_ZONE = worldSettings.getAridZone();

            this.WET_PRECIPITION = worldSettings.getWetPrecipitation();
            this.HUMID_PRECIPITION = worldSettings.getHumidPrecipitation();
            this.SEMI_ARID__PRECIPITION = worldSettings.getSemiAridPrecipitation();
            this.ARID_PRECIPITION = worldSettings.getAridPrecipitation();
        }

        @Transactional
        private void procesLandClimate(Climate climate) {
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

        @Transactional
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

}