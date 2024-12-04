package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
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

    public Aircurrent(Climate startingClimate, Climate endingClimate, AircurrentType aircurrentType, int currentStrength) {
        this.aircurrentType = aircurrentType;
        this.currentStrength = currentStrength;

        this.startingClimate = startingClimate;
        this.endingClimate = endingClimate;

        startingClimate.getOutgoingAircurrents().add(this);
        endingClimate.getIncomingAircurrents().add(this);
    }

    public void transfer(double transferPerStrength) {
        endingClimate.addIncomingMoisture(transferPerStrength * currentStrength);
    }


    public Aircurrent createOutgoingClone(Climate climateClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(this.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setStartingClimate(climateClone);
        clone.setEndingClimate(this.endingClimate);
        return clone;
    }

    public Aircurrent createIncommingClone(Climate climateClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(this.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setEndingClimate(climateClone);
        clone.setStartingClimate(this.startingClimate);
        return clone;
    }

    @Override
    public String toString() {
        return "Aircurrent{" +
                "id=" + id +
                ", currentStrength=" + currentStrength +
                ", aircurrentType=" + aircurrentType +
                '}';
    }

    public double getHeatTransfer(Climate climate, int incomingAirPressure) {
        double averageTemperature = (climate.getMeanTemperature() + this.startingClimate.getMeanTemperature()) / 2d;
        double heatChange = (averageTemperature - climate.getMeanTemperature());
        return heatChange / incomingAirPressure * currentStrength;
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
