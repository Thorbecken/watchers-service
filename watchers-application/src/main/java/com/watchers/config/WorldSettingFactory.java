package com.watchers.config;

import com.watchers.model.world.WorldSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WorldSettingFactory {

    // World configuration
    private final long xSize;
    private final long ySize;
    private final boolean lifePreSeeded;
    private final int coastalZone;
    private final int oceanicZone;
    private final int numberOfContinents;
    private final int continentalToOcceanicRatio;
    private final int continentalContinentWeight;

    // Continentalshift configuration
    private final int driftVelocity;
    private final int drifFlux;
    private final long heigtDivider;
    private final int minimumContinents;
    private final int maximumContinents;
    private final int maxContinentSize;
    private final int maxWidthLenghtBalance;

    // Erosion configuration
    private final int minHeightDifference;
    private final int maxErosion;

    public WorldSettingFactory(
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
            @Value("${watch.erosion.max}") int maxErosion) {
        this.xSize = xSize;
        this.ySize = ySize;

        this.minimumContinents = minimumContinents;
        this.maximumContinents = maximumContinents;
        this.maxWidthLenghtBalance = maxWidthLenghtBalance;
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

    public WorldSettings createWorldSetting() {
        return new WorldSettings(
                null,
                null,

                // world generation settings
                xSize,
                ySize,
                numberOfContinents,
                lifePreSeeded,
                coastalZone,
                oceanicZone,
                continentalToOcceanicRatio,
                continentalContinentWeight,

                // Continental settings
                driftVelocity,
                drifFlux,
                heigtDivider,
                minimumContinents,
                maximumContinents,
                maxContinentSize,
                maxWidthLenghtBalance,

                // Erosion configuration
                minHeightDifference,
                maxErosion,

                // Climate settings
                7,
                1
        );
    }
}
