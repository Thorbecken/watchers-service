package com.watchers.manager;

import com.watchers.model.world.WorldSetting;
import com.watchers.model.enums.WorldStatusEnum;
import com.watchers.repository.WorldSettingsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WorldSettingManager {

    private WorldSettingsRepository worldSettingsRepository;

    @Transactional
    public void changeContinentalSetting(Long worldId, boolean newValue){
        Optional<WorldSetting> optionalWorldSetting =  worldSettingsRepository.findById(worldId);
        if(optionalWorldSetting.isPresent()){
            WorldSetting worldSetting = optionalWorldSetting.get();
            worldSetting.setNeedsContinentalShift(newValue);
            worldSettingsRepository.saveAndFlush(worldSetting);
        }
    }

    @Transactional
    public List<Long> getAllWaitingWorldSettings() {
        return worldSettingsRepository.findAll().stream()
                .filter(worldSetting -> worldSetting.getWorldStatusEnum().equals(WorldStatusEnum.WAITING))
                .map(WorldSetting::getWorldId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setWorldInProgress(Long worldId) {
        Optional<WorldSetting> optionalWorldSetting = worldSettingsRepository.findById(worldId);
        if(optionalWorldSetting.isPresent()) {
            WorldSetting worldSetting = optionalWorldSetting.get();
            worldSetting.setWorldStatusEnum(WorldStatusEnum.IN_PROGRESS);
            worldSettingsRepository.save(worldSetting);
            worldSettingsRepository.flush();
        }
    }

    @Transactional
    public void setWorldInWaiting(Long worldId) {
        Optional<WorldSetting> optionalWorldSetting = worldSettingsRepository.findById(worldId);
        if(optionalWorldSetting.isPresent()) {
            WorldSetting worldSetting = optionalWorldSetting.get();
            worldSetting.setWorldStatusEnum(WorldStatusEnum.WAITING);
            worldSettingsRepository.save(worldSetting);
            worldSettingsRepository.flush();
        }
    }

    @Transactional
    public void queInTurn() {
        worldSettingsRepository.findAll()
                .forEach(worldSetting -> {
                            worldSetting.setNeedsProcessing(true);
                            worldSettingsRepository.save(worldSetting);
                        }
                );
        worldSettingsRepository.flush();
    }

    @Transactional
    public void queInSave() {
        worldSettingsRepository.findAll()
                .forEach(worldSetting -> {
                            worldSetting.setNeedsSaving(true);
                            worldSettingsRepository.save(worldSetting);
                        }
                );
        worldSettingsRepository.flush();
    }

    @Transactional
    public void queInContinentalshift() {
        worldSettingsRepository.findAll()
                .forEach(worldSetting -> {
                            worldSetting.setNeedsContinentalShift(true);
                            worldSettingsRepository.save(worldSetting);
                        }
                );
        worldSettingsRepository.flush();
    }

    @Transactional
    public WorldSetting getWorldSetting(Long worldId) {
        WorldSetting worldSetting = worldSettingsRepository.getOne(worldId);
        Assert.notNull(worldSetting.getWorldId(), "The worldId was null.");
        Assert.notNull(worldSetting.getWorldStatusEnum(), "The worldStatusEnum was null");
        return worldSetting;
    }

    @Transactional
    public void createNewWorldSetting(long worldId, WorldStatusEnum worldStatusEnum, boolean needsProcessing, boolean needsSaving, boolean needsContinentalshift, long heigtDivider, int minimumContinents) {
        WorldSetting worldSetting = new WorldSetting(worldId, worldStatusEnum, needsProcessing, needsSaving, needsContinentalshift, heigtDivider, minimumContinents);
        worldSettingsRepository.save(worldSetting);
        worldSettingsRepository.flush();
    }

    @Transactional
    public void changeSaveSetting(Long worldId, boolean newValue){
        Optional<WorldSetting> optionalWorldSetting =  worldSettingsRepository.findById(worldId);
        if(optionalWorldSetting.isPresent()){
            WorldSetting worldSetting = optionalWorldSetting.get();
            worldSetting.setNeedsSaving(newValue);
            worldSettingsRepository.saveAndFlush(worldSetting);
        }
    }
}
