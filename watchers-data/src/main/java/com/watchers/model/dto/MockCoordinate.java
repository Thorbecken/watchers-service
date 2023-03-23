package com.watchers.model.dto;

import com.watchers.model.coordinate.Coordinate;
import lombok.Getter;

import java.util.Objects;

@Getter
public class MockCoordinate {
    private final long xCoord;
    private final long yCoord;

    public MockCoordinate(Coordinate coordinate) {
        this.xCoord = coordinate.getXCoord();
        this.yCoord = coordinate.getYCoord();
    }

    public MockCoordinate(long x, long y) {
        this.yCoord = y;
        this.xCoord = x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockCoordinate that = (MockCoordinate) o;
        return xCoord == that.xCoord && yCoord == that.yCoord;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCoord, yCoord);
    }

    @Override
    public String toString() {
        return "MockCoordinate{" +
                "xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                '}';
    }
}
