package com.watchers.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
    private String directory;

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
    private int maximumContinents;
    private int continentalToOcceanicRatio;
    private int maxContinentSize;
    private int continentalContinentWeight;
    private int maxWidthLenghtBalance;

    // Erosion configuration
    private int minHeightDifference;
    private int maxErosion;

    public SettingConfiguration(
            Environment environment,

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
            @Value("${watch.continentalshift.maximumContinents:30}") int maximumContinents,
            @Value("${watch.continentalshift.maxWidthLenghtBalance:3}") int maxWidthLenghtBalance,
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
        this.directory = environment.getProperty("WATCHERS_SAVE_PATH");

        this.minimumContinents = minimumContinents;
        this.maximumContinents = maximumContinents;
        this.maxWidthLenghtBalance = maxWidthLenghtBalance;
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
