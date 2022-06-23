package com.watchers.manager;

import com.watchers.model.world.WorldMetaData;
import com.watchers.model.enums.WorldStatusEnum;
import com.watchers.model.world.WorldTypeEnum;
import com.watchers.repository.WorldMetaDataRepository;
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

    private final WorldMetaDataRepository worldMetaDataRepository;

    @Transactional
    public void changeContinentalSetting(Long worldId, boolean newValue) {
        Optional<WorldMetaData> optionalWorldSetting = worldMetaDataRepository.findById(worldId);
        if (optionalWorldSetting.isPresent()) {
            WorldMetaData worldMetaData = optionalWorldSetting.get();
            worldMetaData.setNeedsContinentalShift(newValue);
            worldMetaDataRepository.saveAndFlush(worldMetaData);
        }
    }

    @Transactional
    public List<Long> getAllWaitingWorldSettings() {
        List<WorldMetaData> worldMetaDataList = worldMetaDataRepository.findAll();

        return worldMetaDataList.stream()
                .filter(worldSetting -> worldSetting.getWorldStatusEnum().equals(WorldStatusEnum.WAITING))
                .map(WorldMetaData::getId)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setWorldInProgress(Long worldId) {
        Optional<WorldMetaData> optionalWorldSetting = worldMetaDataRepository.findById(worldId);
        if (optionalWorldSetting.isPresent()) {
            WorldMetaData worldMetaData = optionalWorldSetting.get();
            worldMetaData.setWorldStatusEnum(WorldStatusEnum.IN_PROGRESS);
            worldMetaDataRepository.save(worldMetaData);
            worldMetaDataRepository.flush();
        }
    }

    @Transactional
    public void setWorldInWaiting(Long worldId) {
        Optional<WorldMetaData> optionalWorldSetting = worldMetaDataRepository.findById(worldId);
        if (optionalWorldSetting.isPresent()) {
            WorldMetaData worldMetaData = optionalWorldSetting.get();
            worldMetaData.setWorldStatusEnum(WorldStatusEnum.WAITING);
            worldMetaDataRepository.save(worldMetaData);
            worldMetaDataRepository.flush();
        }
    }

    @Transactional
    public void queInTurn() {
        List<WorldMetaData> worldMetaDataList = worldMetaDataRepository.findAll();

        worldMetaDataList.forEach(worldSetting -> worldSetting.setNeedsProcessing(true));
    }

    @Transactional
    public void queInSave() {
        List<WorldMetaData> worldMetaDataList = worldMetaDataRepository.findAll();

        worldMetaDataList.forEach(worldSetting -> worldSetting.setNeedsSaving(true));
    }

    @Transactional
    public void queInContinentalshift() {
        List<WorldMetaData> worldMetaDataList = worldMetaDataRepository.findAll();

        worldMetaDataList.forEach(worldSetting -> worldSetting.setNeedsContinentalShift(true));
    }

    @Transactional
    public WorldMetaData getWorldSetting(Long worldId) {
        WorldMetaData worldMetaData = worldMetaDataRepository.getOne(worldId);
        Assert.notNull(worldMetaData.getId(), "The worldId was null.");
        Assert.notNull(worldMetaData.getWorldStatusEnum(), "The worldStatusEnum was null");
        Assert.notNull(worldMetaData.getWorldTypeEnum(), "The WorldTypeEnum was null");
        return worldMetaData;
    }

    public WorldMetaData createNewWorldSetting(long worldId, WorldStatusEnum worldStatusEnum, WorldTypeEnum worldTypeEnum, boolean needsProcessing, boolean needsSaving, boolean needsContinentalshift) {
        return new WorldMetaData(worldId, null, worldStatusEnum, worldTypeEnum, needsProcessing, needsSaving, needsContinentalshift,1,1,1);
    }

    @Transactional
    public void changeSaveSetting(Long worldId, boolean newValue) {
        Optional<WorldMetaData> optionalWorldSetting = worldMetaDataRepository.findById(worldId);
        if (optionalWorldSetting.isPresent()) {
            WorldMetaData worldMetaData = optionalWorldSetting.get();
            worldMetaData.setNeedsSaving(newValue);
            worldMetaDataRepository.saveAndFlush(worldMetaData);
        }
    }

    public void saveTime(WorldMetaData data) {
        worldMetaDataRepository.findById(data.getId()).ifPresent(worldMetaData -> {
            worldMetaData.setEpoch(data.getEpoch());
            worldMetaData.setEra(data.getEra());
            worldMetaData.setAge(data.getAge());
            worldMetaDataRepository.saveAndFlush(worldMetaData);
        });
    }
}
