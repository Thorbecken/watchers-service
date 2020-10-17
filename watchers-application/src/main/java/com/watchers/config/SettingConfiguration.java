package com.watchers.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class SettingConfiguration {

    // Service configuration
    private boolean persistent;
    private int processingTimer;
    private int turnTimer;
    private int continentalshiftTimer;
    private int saveTimer;

    // World configuration
    private long xSize;
    private long ySize;
    private int numberOfContinents;
    private boolean lifePreSeeded;
    private int coastalZone;
    private int oceanicZone;

    // Continentalshift configuration
    private int driftVelocity;
    private int drifFlux;
    private long heigtDivider;
    private int minimumContinents;
    private int continentalToOcceanicRatio;
    private int maxContinentSize;
    private int continentalContinentWeight;

    // Erosion configuration
    private int minHeightDifference;
    private int maxErosion;

    public SettingConfiguration(
            @Value("${watch.startup.persistent}") boolean persistent,
            @Value("${watch.startup.processingTimer}") int processingTimer,
            @Value("${watch.startup.turnTimer}") int turnTimer,
            @Value("${watch.startup.continentalshiftTimer}") int continentalshiftTimer,
            @Value("${watch.startup.saveTimer}") int saveTimer,

            @Value("${watch.worldsettings.xSize}") long xSize,
            @Value("${watch.worldsettings.ySize}") long ySize,
            @Value("${watch.worldsettings.numberOfContinents}") int numberOfContinents,
            @Value("${watch.worldsettings.lifePresSeeded}") boolean lifePreSeeded,
            @Value("${watch.worldsettings.coastalZone}") int coastalZone,
            @Value("${watch.worldsettings.oceanicZone}") int oceanicZone,

            @Value("${watch.continentalshift.driftVelocity}") int driftVelocity,
            @Value("${watch.continentalshift.driftFlux}") int drifFlux,
            @Value("${watch.continentalshift.heightdivider}") long heigtDivider,
            @Value("${watch.continentalshift.minContinents}") int minimumContinents,
            @Value("${watch.continentalshift.maxContinentsize:0}") int maxContinentSize,
            @Value("${watch.continentalshift.continentalToOcceanicRatio}") int continentalToOcceanicRatio,
            @Value("${watch.continentalshift.continentalContinentWeight}") int continentalContinentWeight,
            @Value("${watch.erosion.minHeightDifference}") int minHeightDifference,
            @Value("${watch.erosion.max}") int maxErosion){
        this.persistent = persistent;
        this.processingTimer = processingTimer;
        this.turnTimer = turnTimer;
        this.continentalshiftTimer = continentalshiftTimer;
        this.saveTimer = saveTimer;

        this.minimumContinents = minimumContinents;
        this.xSize = xSize;
        this.ySize = ySize;
        this.numberOfContinents = numberOfContinents;
        this.lifePreSeeded = lifePreSeeded;
        this.coastalZone = coastalZone;
        this.oceanicZone = oceanicZone;

        this.driftVelocity = driftVelocity;
        this.drifFlux = drifFlux;
        this.heigtDivider = heigtDivider;
        this.maxContinentSize = maxContinentSize;
        this.continentalToOcceanicRatio = continentalToOcceanicRatio;
        this.continentalContinentWeight = continentalContinentWeight;

        this.minHeightDifference = minHeightDifference;
        this.maxErosion = maxErosion;
    }
}
