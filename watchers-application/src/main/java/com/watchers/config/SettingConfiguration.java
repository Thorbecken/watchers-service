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
    private long heigtDivider;
    private int minimumContinents;

    public SettingConfiguration(
            @Value("${watch.heightdivider}") long heigtDivider,
            @Value("${watch.minContinents}") int minimumContinents,
            @Value("${watch.worldGeneration.xSize}") long xSize,
            @Value("${watch.worldGeneration.ySize}") long ySize,
            @Value("${watch.worldGeneration.numberOfContinents}") int numberOfContinents,

            @Value("${startup.persistent}") boolean persistent,
            @Value("${startup.processingTimer}") int processingTimer,
            @Value("${startup.turnTimer}") int turnTimer,
            @Value("${startup.continentalshiftTimer}") int continentalshiftTimer,
            @Value("${startup.saveTimer}") int saveTimer){
        this.heigtDivider = heigtDivider;
        this.minimumContinents = minimumContinents;
        this.xSize = xSize;
        this.ySize = ySize;
        this.numberOfContinents = numberOfContinents;

        this.persistent = persistent;
        this.processingTimer = processingTimer;
        this.turnTimer = turnTimer;
        this.continentalshiftTimer = continentalshiftTimer;
        this.saveTimer = saveTimer;
    }
}
