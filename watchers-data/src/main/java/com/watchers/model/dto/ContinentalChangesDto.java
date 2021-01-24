package com.watchers.model.dto;

import com.watchers.model.coordinate.Coordinate;
import lombok.Data;

@Data
public class ContinentalChangesDto {
    private boolean empty;
    private Coordinate key;
    private MockTile mockTile;
    private MockContinent newMockContinent;

    public ContinentalChangesDto(Coordinate coordinate){
        this.key = coordinate;
    }
}