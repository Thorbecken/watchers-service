package com.watchers.manager;

import com.watchers.components.continentaldrift.*;
import com.watchers.helper.StopwatchTimer;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ContinentalDriftManager {

    private ContinentalDriftPredicter continentalDriftPredicter;
    private ContinentalDriftTileChangeComputer continentalDriftTileChangeComputer;
    private ContinentalDriftDirectionChanger continentalDriftDirectionChanger;
    private ContinentalDriftWorldAdjuster continentalDriftWorldAdjuster;
    private ContinentalDriftNewTileAssigner continentalDriftNewTileAssigner;
    private ContinentalCorrector continentalCorrector;
    private SurfaceTypeComputator surfaceTypeComputator;
    private ErosionAdjuster erosionAdjuster;
    private WorldSettingManager worldSettingManager;
    private ContinentalIntegretyAdjuster continentalIntegretyAdjuster;
    private ContinentalSplitter continentalSplitter;
    private ContinentalMerger continentalMerger;

    public void process(ContinentalDriftTaskDto taskDto){
        StopwatchTimer.start();
        continentalDriftDirectionChanger.process(taskDto);
        StopwatchTimer.stop("continentalDriftDirectionChanger");
        StopwatchTimer.start();
        continentalDriftPredicter.process(taskDto);
        StopwatchTimer.stop("continentalDriftPredicter");
        StopwatchTimer.start();
        continentalDriftTileChangeComputer.process(taskDto);
        StopwatchTimer.stop("continentalDriftTileChangeComputer");
        StopwatchTimer.start();
        continentalDriftNewTileAssigner.process(taskDto);
        StopwatchTimer.stop("continentalDriftNewTileAssigner");
        StopwatchTimer.start();
        continentalDriftWorldAdjuster.process(taskDto);
        StopwatchTimer.stop("continentalDriftWorldAdjuster");
        StopwatchTimer.start();
        continentalCorrector.process(taskDto);
        StopwatchTimer.stop("continentalCorrector");
        StopwatchTimer.start();
        continentalIntegretyAdjuster.process(taskDto);
        StopwatchTimer.stop("continentalIntegretyAdjuster");
        StopwatchTimer.start();
        continentalSplitter.process(taskDto);
        StopwatchTimer.stop("continentalSplitter");
        StopwatchTimer.start();
        continentalMerger.process(taskDto);
        StopwatchTimer.stop("continentalMerger");
        StopwatchTimer.start();
        erosionAdjuster.process(taskDto);
        StopwatchTimer.stop("erosionAdjuster");
        StopwatchTimer.start();
        erosionAdjuster.process(taskDto);
        StopwatchTimer.stop("erosionAdjuster");
        StopwatchTimer.start();
        surfaceTypeComputator.process(taskDto);
        StopwatchTimer.stop("surfaceTypeComputator");

        worldSettingManager.changeContinentalSetting(taskDto.getWorldId(), false);
        log.trace("Proccesed a continentaldrift for world id: " + taskDto.getWorldId());
    }

}
