package com.watchers.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.special.crystal.TectonicCrystal;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Objects;

@Data
@Entity
@JsonSerialize
@NoArgsConstructor
@Table(name = "directions")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Direction {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("directionId")
    @SequenceGenerator(name = "Direction_Gen", sequenceName = "Direction_Seq", allocationSize = 1)
    @GeneratedValue(generator = "Direction_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "direction_id")
    private Long id;

    @JsonView(Views.Public.class)
    @Column(name = "x_velocity")
    private int xVelocity;

    @JsonView(Views.Public.class)
    @Column(name = "y_velocity")
    private int yVelocity;

    @JsonView(Views.Internal.class)
    @Column(name = "x_drift_pressure")
    private long xDriftPressure;

    @JsonView(Views.Internal.class)
    @Column(name = "y_drift_pressure")
    private long yDriftPressure;

    public void resetPressures() {
        this.yDriftPressure = 0;
        this.xDriftPressure = 0;
    }

    public void adjustPressureFromIncomingDirection(Direction incomingDirection) {
        long xDifference = incomingDirection.getXVelocity() - this.getXVelocity();
        long yDifference = incomingDirection.getYVelocity() - this.getYVelocity();

        this.setXDriftPressure(this.getXDriftPressure() + xDifference);
        this.setYDriftPressure(this.getYDriftPressure() + yDifference);
    }

    public void setVelocityFromPressure(int maxDrift) {
        assert maxDrift >= 0;
        this.adjustXPressure(maxDrift);
        this.adjustYPressure(maxDrift);
    }

    private void adjustXPressure(int maxDrift) {
        if (xDriftPressure < 0 && xVelocity > -maxDrift) {
            this.xVelocity--;
        } else if (xDriftPressure > 0 && xVelocity < maxDrift) {
            this.xVelocity++;
        }
    }

    private void adjustYPressure(int maxDrift) {
        if (yDriftPressure < 0 && yVelocity > -maxDrift) {
            this.yVelocity--;
        } else if (yDriftPressure > 0 && yVelocity < maxDrift) {
            this.yVelocity++;
        }
    }

    public Direction(int xVelocity, int yVelocity) {
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    public Direction createClone() {
        Direction clone = new Direction();
        clone.setId(this.id);
        clone.setXVelocity(this.xVelocity);
        clone.setYVelocity(this.yVelocity);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Direction direction = (Direction) o;
        return Objects.equals(id, direction.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void addPressure(TectonicCrystal tectonicCrystal, Coordinate coordinate, int maxDrift) {
        long adjustedXDifference = coordinate.getAdjustedXDistance(tectonicCrystal.getCoordinate());
        long adjustedYDifference = coordinate.getAdjustedYDistance(tectonicCrystal.getCoordinate());

        if (Math.abs(adjustedXDifference) < Math.abs(adjustedYDifference)) {
            if (adjustedXDifference > 0 && xVelocity < maxDrift) {
                this.xVelocity++;
            } else if (xVelocity > -maxDrift) {
                this.xVelocity--;
            }
        } else {
            if (adjustedYDifference > 0 && yVelocity < -maxDrift) {
                this.yVelocity++;
            } else if (yVelocity > -maxDrift){
                this.yVelocity--;
            }
        }
    }
}
