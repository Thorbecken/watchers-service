package com.watchers.model.dto;

import lombok.Data;

@Data
public class ContinentalChangesDto {
    private boolean empty;
    private MockCoordinate key;
    private MockTile mockTile;
    private MockContinentDto mockContinentDto;

    public ContinentalChangesDto(MockCoordinate mockCoordinate){
        this.key = mockCoordinate;
    }
}