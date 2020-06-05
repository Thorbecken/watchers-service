package com.watchers.model.dto;

import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.MockContinent;
import com.watchers.model.environment.Tile;
import lombok.Data;

@Data
public class ContinentalChangesDto {
    private boolean empty;
    private Coordinate key;
    private Coordinate oldCoordinate;
    private Tile newTile;
    private MockContinent newMockContinent;

    public ContinentalChangesDto(Coordinate coordinate){
        this.key = coordinate;
    }
}
