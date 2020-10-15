package com.watchers.manager;

import com.watchers.model.WorldSetting;
import com.watchers.model.WorldStatusEnum;
import com.watchers.repository.inmemory.WorldSettingsRepositoryInMemory;
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

    private WorldSettingsRepositoryInMemory worldSettingsRepositoryInMemory;

    @Transactional("inmemoryDatabaseTransactionManager")
    public void changeContinentalSetting(Long worldId, boolean newValue){
        Optional<WorldSetting> optionalWorldSetting =  worldSettingsRepositoryInMemory.findById(worldId);
        if(optionalWorldSetting.isPresent()){
            WorldSetting worldSetting = optionalWorldSetting.get();
            worldSetting.setNeedsContinentalShift(newValue);
            worldSettingsRepositoryInMemory.saveAndFlush(worldSetting);
        }
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public List<Long> getAllWaitingWorldSettings() {
        return worldSettingsRepositoryInMemory.findAll().stream()
                .filter(worldSetting -> worldSetting.getWorldStatusEnum().equals(WorldStatusEnum.WAITING))
                .map(WorldSetting::getWorldId)
                .collect(Collectors.toList());
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void setWorldInProgress(Long worldId) {
        Optional<WorldSetting> optionalWorldSetting = worldSettingsRepositoryInMemory.findById(worldId);
        if(optionalWorldSetting.isPresent()) {
            WorldSetting worldSetting = optionalWorldSetting.get();
            worldSetting.setWorldStatusEnum(WorldStatusEnum.IN_PROGRESS);
            worldSettingsRepositoryInMemory.save(worldSetting);
            worldSettingsRepositoryInMemory.flush();
        }
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void setWorldInWaiting(Long worldId) {
        Optional<WorldSetting> optionalWorldSetting = worldSettingsRepositoryInMemory.findById(worldId);
        if(optionalWorldSetting.isPresent()) {
            WorldSetting worldSetting = optionalWorldSetting.get();
            worldSetting.setWorldStatusEnum(WorldStatusEnum.WAITING);
            worldSettingsRepositoryInMemory.save(worldSetting);
            worldSettingsRepositoryInMemory.flush();
        }
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void queInTurn() {
        worldSettingsRepositoryInMemory.findAll()
                .forEach(worldSetting -> {
                            worldSetting.setNeedsProcessing(true);
                            worldSettingsRepositoryInMemory.save(worldSetting);
                        }
                );
        worldSettingsRepositoryInMemory.flush();
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void queInSave() {
        worldSettingsRepositoryInMemory.findAll()
                .forEach(worldSetting -> {
                            worldSetting.setNeedsSaving(true);
                            worldSettingsRepositoryInMemory.save(worldSetting);
                        }
                );
        worldSettingsRepositoryInMemory.flush();
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void queInContinentalshift() {
        worldSettingsRepositoryInMemory.findAll()
                .forEach(worldSetting -> {
                            worldSetting.setNeedsContinentalShift(true);
                            worldSettingsRepositoryInMemory.save(worldSetting);
                        }
                );
        worldSettingsRepositoryInMemory.flush();
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public WorldSetting getWorldSetting(Long worldId) {
        WorldSetting worldSetting = worldSettingsRepositoryInMemory.getOne(worldId);
        Assert.notNull(worldSetting.getWorldId(), "The worldId was null.");
        Assert.notNull(worldSetting.getWorldStatusEnum(), "The worldStatusEnum was null");
        return worldSetting;
    }

    @Transactional("inmemoryDatabaseTransactionManager")
    public void createNewWorldSetting(long worldId, WorldStatusEnum worldStatusEnum, boolean needsProcessing, boolean needsSaving, boolean needsContinentalshift, long heigtDivider, int minimumContinents) {
        WorldSetting worldSetting = new WorldSetting(worldId, worldStatusEnum, needsProcessing, needsSaving, needsContinentalshift, heigtDivider, minimumContinents);
        worldSettingsRepositoryInMemory.save(worldSetting);
        worldSettingsRepositoryInMemory.flush();
    }

}
