package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@NoArgsConstructor
@Table(name = "aircurrent")
@SequenceGenerator(name = "AC_Gen", sequenceName = "AC_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Aircurrent {

    @Id
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator = "AC_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "aircurrent_id", nullable = false)
    private Long id;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private Climate endingClimate;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private Climate startingClimate;

    @JsonView(Views.Public.class)
    private int currentStrength;

    @JsonView(Views.Public.class)
    private AircurrentType aircurrentType;

    @JsonView(Views.Public.class)
    private long heightDifference;

    public Aircurrent(Climate startingClimate, Climate endingClimate, AircurrentType aircurrentType, int currentStrength) {
        this.aircurrentType = aircurrentType;
        this.currentStrength = currentStrength;

        this.startingClimate = startingClimate;
        this.endingClimate = endingClimate;

        startingClimate.getOutgoingAircurrents().add(this);
        endingClimate.getIncommingAircurrents().add(this);

        recalculateHeigthDifference();
    }

    public void transfer(double amountPerStrength) {
        double amount = amountPerStrength * currentStrength;
        double heightAmount = calculateHeightDifferenceEffect(amount);

        endingClimate.addIncommingMoisture(amount);
        startingClimate.addAirMoistureLossage(heightAmount);
    }

    public double calculateHeightDifferenceEffect(double airMoisture) {
        if (airMoisture > 0L && heightDifference > 0L) {
            if (airMoisture > heightDifference) {
                return heightDifference;
            } else {
                return airMoisture;
            }
        } else {
            return 0;
        }
    }

    public Aircurrent createOutgoingClone(Climate climateClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(this.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setStartingClimate(climateClone);
        clone.setEndingClimate(this.endingClimate);
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    public Aircurrent createIncommingClone(Climate climateClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(this.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setEndingClimate(climateClone);
        clone.setStartingClimate(this.startingClimate);
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    public void recalculateHeigthDifference() {
        Climate endingClimate = this.getEndingClimate();
        Coordinate endingCoordinate = endingClimate.getCoordinate();
        Tile endingTile = endingCoordinate.getTile();
        long endingHeight = endingTile.getHeight();

        Climate startingClimate = this.getStartingClimate();
        Coordinate startingCoordinate = startingClimate.getCoordinate();
        Tile startingTile = startingCoordinate.getTile();
        long startingHeight = startingTile.getHeight();

        this.heightDifference = endingHeight - startingHeight;
    }

    @Override
    public String toString() {
        return "Aircurrent{" +
                "id=" + id +
                ", currentStrength=" + currentStrength +
                ", aircurrentType=" + aircurrentType +
                ", heightDifference=" + heightDifference +
                '}';
    }

    public double getHeatTransfer(Climate climate, int incommingAirPressure) {
        double averageTemperature = (climate.getMeanTemperature() + this.startingClimate.getMeanTemperature()) / 2d;
        double heatChange = (averageTemperature - climate.getMeanTemperature());
        return heatChange / incommingAirPressure * currentStrength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aircurrent that = (Aircurrent) o;
        return Objects.equals(id, that.id)
                && aircurrentType.equals(that.aircurrentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, aircurrentType);
    }
}
