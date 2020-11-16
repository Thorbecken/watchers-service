package com.watchers.manager;

import com.watchers.model.world.WorldSetting;
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
                    WorldSetting worldSetting = worldSettingManager.getWorldSetting(worldId);
                    worldSettingManager.setWorldInProgress(worldId);

                    WorldTaskDto worldTaskDto = getNewWorldTask(worldSetting);
                    worldService.processTurn(worldTaskDto);

                    worldSettingManager.setWorldInWaiting(worldId);
                    log.info(generateLogMessage(worldSetting, stopWatch));
                }
        );
    }

    private String generateLogMessage(WorldSetting worldSetting, StopWatch stopWatch) {
        String  logMessage ="Processing a turn ";
        if(worldSetting.isNeedsContinentalShift()){
            logMessage = logMessage + "and continentalshifting ";
        }
        if(worldSetting.isNeedsSaving()){
            logMessage = logMessage + "and saving ";
        }

        stopWatch.stop();
        logMessage = logMessage + "for world " + worldSetting.getWorldId() + ", and took " + stopWatch.getTotalTimeSeconds() + " seconds.";
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

    private WorldTaskDto getNewWorldTask(WorldSetting worldSetting) {
        if(worldSetting.isNeedsContinentalShift()){
            return new ContinentalDriftTaskDto(worldSetting);
        } else {
            return new WorldTaskDto(worldSetting);
        }
    }

}
