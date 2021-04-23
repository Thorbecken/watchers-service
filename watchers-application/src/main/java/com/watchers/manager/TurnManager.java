package com.watchers.manager;

import com.watchers.model.world.WorldMetaData;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.service.WorldService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@Slf4j
@Service
@AllArgsConstructor
public class TurnManager {

    private WorldSettingManager worldSettingManager;
    private WorldService worldService;

    public void processTurn(){
        worldSettingManager.getAllWaitingWorldSettings().forEach(
                worldId -> {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    WorldMetaData worldMetaData = worldSettingManager.getWorldSetting(worldId);
                    worldSettingManager.setWorldInProgress(worldId);

                    WorldTaskDto worldTaskDto = getNewWorldTask(worldMetaData);
                    worldService.processTurn(worldTaskDto);

                    worldSettingManager.setWorldInWaiting(worldId);
                    log.info(generateLogMessage(worldMetaData, stopWatch));
                }
        );
    }

    private String generateLogMessage(WorldMetaData worldMetaData, StopWatch stopWatch) {
        String  logMessage ="Processing a turn ";
        if(worldMetaData.isNeedsContinentalShift()){
            logMessage = logMessage + "and continentalshifting ";
        }
        if(worldMetaData.isNeedsSaving()){
            logMessage = logMessage + "and saving ";
        }

        stopWatch.stop();
        logMessage = logMessage + "for world " + worldMetaData.getId() + ", and took " + stopWatch.getTotalTimeSeconds() + " seconds.";
        return logMessage;
    }

    public void queInTurn(){
        worldSettingManager.queInTurn();
    }

    public void queInSave(){
        worldSettingManager.queInSave();
    }

    public void queInContinentalshift(){
        worldSettingManager.queInContinentalshift();
    }

    private WorldTaskDto getNewWorldTask(WorldMetaData worldMetaData) {
        if(worldMetaData.isNeedsContinentalShift()){
            return new ContinentalDriftTaskDto(worldMetaData);
        } else {
            return new WorldTaskDto(worldMetaData);
        }
    }

}
