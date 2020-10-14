package com.watchers.manager;

import com.watchers.model.WorldSettings;
import com.watchers.model.WorldStatusEnum;
import com.watchers.model.dto.ContinentalDriftTaskDto;
import com.watchers.model.dto.WorldTaskDto;
import com.watchers.repository.inmemory.WorldSettingsRepositoryInMemory;
import com.watchers.service.WorldService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TurnManager {

    private WorldSettingsRepositoryInMemory worldSettingsRepositoryInMemory;
    private WorldService worldService;

    public void processTurn(Long worldId){
        WorldSettings worldSettings = worldSettingsRepositoryInMemory.findById(worldId).orElseThrow(() -> new RuntimeException("World status " + worldId + " was lost in memory."));
        if(!worldSettings.getWorldStatusEnum().equals(WorldStatusEnum.WAITING)) {
            worldSettings.setWorldStatusEnum(WorldStatusEnum.IN_PROGRESS);
            worldSettingsRepositoryInMemory.save(worldSettings);
            worldSettingsRepositoryInMemory.flush();

            WorldTaskDto worldTaskDto = getNewWorldTask(worldSettings);
            worldService.processTurn(worldTaskDto);

            WorldSettings newWorldSettings = worldSettingsRepositoryInMemory.findById(worldId).orElseThrow(() -> new RuntimeException("World status " + worldId + " was lost in memory."));
            if(newWorldSettings.getWorldStatusEnum().equals(WorldStatusEnum.IN_PROGRESS)) {
                newWorldSettings.setWorldStatusEnum(WorldStatusEnum.WAITING);
                worldSettingsRepositoryInMemory.save(newWorldSettings);
            }
        }
    }

    public void queInTurn(Long worldId){
        WorldSettings worldSettings = worldSettingsRepositoryInMemory.findById(worldId).orElseThrow(() -> new RuntimeException("World status " + worldId + " was lost in memory."));
        worldSettings.setNeedsProcessing(true);
        worldSettingsRepositoryInMemory.save(worldSettings);
    }

    public void queInSave(Long worldId){
        WorldSettings worldSettings = worldSettingsRepositoryInMemory.findById(worldId).orElseThrow(() -> new RuntimeException("World status " + worldId + " was lost in memory."));
        worldSettings.setNeedsSaving(true);
        worldSettingsRepositoryInMemory.save(worldSettings);
    }

    public void queInContinentalshift(Long worldId){
        WorldSettings worldSettings = worldSettingsRepositoryInMemory.findById(worldId).orElseThrow(() -> new RuntimeException("World status " + worldId + " was lost in memory."));
        worldSettings.setNeedsContinentalShift(true);
        worldSettingsRepositoryInMemory.save(worldSettings);
    }

    private WorldTaskDto getNewWorldTask(WorldSettings worldSettings) {
        if(worldSettings.isNeedsContinentalShift()){
            return new ContinentalDriftTaskDto(worldSettings);
        } else {
            return new WorldTaskDto(worldSettings);
        }
    }

}
