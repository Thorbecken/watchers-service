package com.watchers.manager;

import com.watchers.components.continentaldrift.*;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.crystal.HotSpotCrystal;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalDriftManager {

    private ContinentalMantelPlumeComputer continentalMantelPlumeComputer;
    private ContinentalDriftPredicter continentalDriftPredicter;
    private ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private ContinentalCorrector continentalCorrector;
    private SurfaceTypeComputer surfaceTypeComputer;
    private ContinentalHotSpotComputer continentalHotSpotComputer;
    private ErosionAdjuster erosionAdjuster;
    private WorldSettingManager worldSettingManager;
    private ContinentalIntegrityAdjuster continentalIntegrityAdjuster;
    private ContinentalSplitter continentalSplitter;
    private ContinentalMerger continentalMerger;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        World world = taskDto.getWorld();
        Long worldHeight = world.getWorldHeight() + world.getCoordinates().stream()
                .map(Coordinate::getTile)
                .map(Tile::getPointOfInterest)
                .filter(pointOfInterest -> pointOfInterest instanceof HotSpotCrystal)
                .map(pointOfInterest -> ((HotSpotCrystal) pointOfInterest))
                .mapToLong(HotSpotCrystal::getHeightBuildup)
                .sum();

        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalMantelPlumeComputer, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalDriftDirectionChanger, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalDriftPredicter, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalDriftTileChangeComputer, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalDriftNewTileAssigner, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalDriftWorldAdjuster, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalCorrector, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalIntegrityAdjuster, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalSplitter, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalMerger, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(erosionAdjuster, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(continentalHotSpotComputer, taskDto, worldHeight);
        worldHeight = StopwatchTimer.processTimeAndCheckHeight(erosionAdjuster, taskDto, worldHeight);
        StopwatchTimer.processTimeAndCheckHeight(surfaceTypeComputer, taskDto, worldHeight);

        worldSettingManager.changeContinentalSetting(taskDto.getWorldId(), false);
        taskDto.clearContinentalData();

        log.trace("Proccesed a continentaldrift for world id: " + taskDto.getWorldId());
    }
}
