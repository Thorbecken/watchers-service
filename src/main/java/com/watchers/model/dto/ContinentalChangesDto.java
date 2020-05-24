package com.watchers.model.dto;

import com.watchers.model.environment.Tile;
import lombok.Data;

@Data
public class ContinentalChangesDto {
    private boolean coordinateChanged;
    private boolean empty;
    private Tile newTile;
}
