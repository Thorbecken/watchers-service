package com.watchers.model.dto;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class MockContinentDto {
    private Long continentId;
    private SurfaceType surfaceType;
    private List<Coordinate> coordinateList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockContinentDto that = (MockContinentDto) o;
        return Objects.equals(continentId, that.continentId) && surfaceType == that.surfaceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(continentId, surfaceType);
    }
}
