package com.watchers.manager;

import com.watchers.components.continentaldrift.*;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.world.World;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalDriftManager {

    private ContinentalMantelPlumeProcessor continentalMantelPlumeProcessor;
    private ContinentalDriftPredicter continentalDriftPredicter;
    private ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private ContinentalCorrector continentalCorrector;
    private SurfaceTypeComputator surfaceTypeComputator;
    private ContinentalHotSpotProcessor continentalHotSpotProcessor;
    private ErosionAdjuster erosionAdjuster;
    private WorldSettingManager worldSettingManager;
    private ContinentalIntegretyAdjuster continentalIntegretyAdjuster;
    private ContinentalSplitter continentalSplitter;
    private ContinentalMerger continentalMerger;

    @Transactional
    public void process(ContinentalDriftTaskDto taskDto) {
        Long worldHeight = taskDto.getWorld().getWorldHeight();
        World world = taskDto.getWorld();
        StopwatchTimer.start();
        continentalMantelPlumeProcessor.process(taskDto);
        StopwatchTimer.stop("continentalMantelPlumeProcessor");
        StopwatchTimer.start();
        continentalDriftDirectionChanger.process(taskDto);
        StopwatchTimer.stop("continentalDriftDirectionChanger");
        worldHeight = checkWorldHeight("continentalDriftDirectionChanger",worldHeight, world);
        StopwatchTimer.start();
        continentalDriftPredicter.process(taskDto);
        StopwatchTimer.stop("continentalDriftPredicter");
        worldHeight = checkWorldHeight("continentalDriftPredicter",worldHeight, world);
        StopwatchTimer.start();
        continentalDriftTileChangeComputer.process(taskDto);
        StopwatchTimer.stop("continentalDriftTileChangeComputer");
        StopwatchTimer.start();
        continentalDriftNewTileAssigner.process(taskDto);
        StopwatchTimer.stop("continentalDriftNewTileAssigner");
        StopwatchTimer.start();
        continentalDriftWorldAdjuster.process(taskDto);
        StopwatchTimer.stop("continentalDriftWorldAdjuster");
        worldHeight = checkWorldHeight("continentalDriftWorldAdjuster",worldHeight, world);
        StopwatchTimer.start();
        continentalCorrector.process(taskDto);
        StopwatchTimer.stop("continentalCorrector");
        worldHeight = checkWorldHeight("continentalCorrector",worldHeight, world);
        StopwatchTimer.start();
        continentalIntegretyAdjuster.process(taskDto);
        StopwatchTimer.stop("continentalIntegretyAdjuster");
        worldHeight = checkWorldHeight("continentalIntegretyAdjuster",worldHeight, world);
        StopwatchTimer.start();
        continentalSplitter.process(taskDto);
        StopwatchTimer.stop("continentalSplitter");
        worldHeight = checkWorldHeight("continentalSplitter",worldHeight, world);
        StopwatchTimer.start();
        continentalMerger.process(taskDto);
        StopwatchTimer.stop("continentalMerger");
        worldHeight = checkWorldHeight("continentalMerger",worldHeight, world);
        StopwatchTimer.start();
        erosionAdjuster.process(taskDto);
        StopwatchTimer.stop("erosionAdjuster");
        worldHeight = checkWorldHeight("erosionAdjuster",worldHeight, world);
        StopwatchTimer.start();
        continentalHotSpotProcessor.process(taskDto);
        StopwatchTimer.stop("continentalHotSpotProcessor");
        worldHeight = checkWorldHeight("continentalHotSpotProcessor",worldHeight, world);
        StopwatchTimer.start();
        erosionAdjuster.process(taskDto);
        StopwatchTimer.stop("erosionAdjuster");
        worldHeight = checkWorldHeight("erosionAdjuster",worldHeight, world);
        StopwatchTimer.start();
        surfaceTypeComputator.process(taskDto);
        StopwatchTimer.stop("surfaceTypeComputator");
        worldHeight = checkWorldHeight("surfaceTypeComputator",worldHeight, world);

        worldSettingManager.changeContinentalSetting(taskDto.getWorldId(), false);
        taskDto.clearContinentalData();
        log.trace("Proccesed a continentaldrift for world id: " + taskDto.getWorldId());
    }

    private Long checkWorldHeight(String processor, Long currentHeight, World world){
        Long newHeight = world.getWorldHeight();
        log.info("HeightDeficit: " + world.getHeightDeficit() + " @" + processor);
        if(newHeight > currentHeight){
            log.error("-----------------------------------------------------");
            log.error("-----------------------------------------------------");
            log.error(processor + " changed the current height from " + currentHeight + " to " + newHeight);
            log.error("-----------------------------------------------------");
            log.error("-----------------------------------------------------");
        }
        return newHeight;
    }

}
