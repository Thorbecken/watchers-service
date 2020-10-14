package com.watchers.model.dto;

import com.watchers.model.WorldSettings;
import lombok.Data;

@Data
public class WorldTaskDto {

    private Long worldId;
    boolean saving;
    boolean continentalshift;

    public WorldTaskDto(WorldSettings worldSettings){
        this(worldSettings.getWorldId(), worldSettings.isNeedsSaving(), worldSettings.isNeedsContinentalShift());
    }

    public WorldTaskDto(Long worldId, boolean needsSaving, boolean needsContinentaldrift){
        this.worldId = worldId;
        this.saving = needsSaving;
        this.continentalshift = needsContinentaldrift;
    }
}
