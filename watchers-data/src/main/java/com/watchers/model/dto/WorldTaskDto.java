package com.watchers.model.dto;

import com.watchers.model.world.WorldSetting;
import lombok.Data;

@Data
public class WorldTaskDto {

    private Long worldId;
    boolean saving;
    boolean continentalshift;

    public WorldTaskDto(WorldSetting worldSetting){
        this(worldSetting.getWorldId(), worldSetting.isNeedsSaving(), worldSetting.isNeedsContinentalShift());
    }

    public WorldTaskDto(Long worldId, boolean needsSaving, boolean needsContinentaldrift){
        this.worldId = worldId;
        this.saving = needsSaving;
        this.continentalshift = needsContinentaldrift;
    }
}
