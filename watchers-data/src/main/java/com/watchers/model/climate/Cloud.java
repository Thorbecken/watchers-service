package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "cloud")
@NoArgsConstructor
@SequenceGenerator(name="Cloud_Gen", sequenceName="Cloud_Seq", allocationSize = 1)
public class Cloud {

    @Id
    @JsonIgnore
    @GeneratedValue(generator="Cloud_Gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    long airMoisture;

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private long airMoistureLossage;

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Climate incomingClimate;

    public void setPreviousHeight() {
        this.previousHeight = this.currentClimate.getCoordinate().getTile().getHeight();
    }

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private long previousHeight;

    public void setCurrentHeight() {
        this.currentHeight = this.currentClimate.getCoordinate().getTile().getHeight();
    }

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private long currentHeight;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER)
    private Climate currentClimate;

    public Cloud(Climate climate){
        this.setCurrentClimate(climate);
    }

    public void addAirMoisture(long extraAirmoisture){
        if(extraAirmoisture > 0L) {
            if(extraAirmoisture+this.airMoisture < 100L) {
                this.airMoisture = this.airMoisture + extraAirmoisture;
            } else{
                this.airMoisture = 100L;
            }
        }
    }

    public void reduceAirMoisture(long airmoistureReduction){
        if(airmoistureReduction > 0L) {
            if(this.airMoisture-airmoistureReduction > 0L) {
                this.airMoisture = this.airMoisture - airmoistureReduction;
            } else {
                this.airMoisture = 0L;
            }
        }
    }

    @Override
    public String toString() {
        return "Cloud{" +
                "id=" + id +
                ", airMoisture=" + airMoisture +
                '}';
    }

    public void calculateHeightDifferenceEffect() {
        long difference = currentHeight - previousHeight;
        if(airMoisture > 0L && difference > 0L) {
            airMoistureLossage = airMoistureLossage + difference;
        }
    }

    public void calculateNewMoistureLevel() {
        reduceAirMoisture(airMoistureLossage);
    }
}
