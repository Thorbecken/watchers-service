package com.watchers.manager;

import com.watchers.components.continentaldrift.*;
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
    private TileDefined tileDefined;
    private ErosionAdjuster erosionAdjuster;
    private WorldSettingManager worldSettingManager;

    public void process(ContinentalDriftTaskDto taskDto){
        continentalDriftDirectionChanger.process(taskDto);
        continentalDriftPredicter.process(taskDto);
        continentalDriftTileChangeComputer.process(taskDto);
        continentalDriftNewTileAssigner.process(taskDto);
        continentalDriftWorldAdjuster.process(taskDto);
        continentalCorrector.process(taskDto);
        erosionAdjuster.process(taskDto);
        tileDefined.process(taskDto);

        worldSettingManager.changeContinentalSetting(taskDto.getWorldId(), false);
        log.trace("Proccesed a continentaldrift for world id: " + taskDto.getWorldId());
    }

}
