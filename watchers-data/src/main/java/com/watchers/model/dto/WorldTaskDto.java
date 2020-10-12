package com.watchers.model.dto;

import lombok.Data;

@Data
public class WorldTaskDto {

    private Long worldId;

    public WorldTaskDto(Long worldId){
        this.worldId = worldId;
    }
}
