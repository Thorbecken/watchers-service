package com.watchers.model.dto;

import com.watchers.model.world.WorldMetaData;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorldTaskDto {

    private Long worldId;
    boolean saving;
    boolean continentalshift;

    public WorldTaskDto(WorldMetaData worldMetaData){
        this(worldMetaData.getId(), worldMetaData.isNeedsSaving(), worldMetaData.isNeedsContinentalShift());
    }

    public WorldTaskDto(Long worldId, boolean needsSaving, boolean needsContinentaldrift){
        this.worldId = worldId;
        this.saving = needsSaving;
        this.continentalshift = needsContinentaldrift;
    }
}
