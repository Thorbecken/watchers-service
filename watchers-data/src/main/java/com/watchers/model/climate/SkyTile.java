package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@Table(name = "sky")
@SequenceGenerator(name = "Sky_Gen", sequenceName = "Sky_Seq", allocationSize = 1)
public class SkyTile {

    @Id
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator = "Sky_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "sky_id", nullable = false)
    private Long id;

    @JsonProperty("airMoisture")
    @Column(name = "air_moisture")
    @JsonView(Views.Public.class)
    private double airMoisture;

    public void setAirMoisture(double airMoisture) {
        this.airMoisture = airMoisture;
    }

    @Transient
    private double incommingMoisture;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Climate climate;

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private double airMoistureLossage;

    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "startingSky", cascade=CascadeType.ALL)
    private Set<Aircurrent> outgoingAircurrents = new HashSet<>();

    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "endingSky", cascade=CascadeType.ALL)
    private Set<Aircurrent> incommingAircurrents = new HashSet<>();

    public SkyTile(Climate climate) {
        this.climate = climate;
    }

    @JsonIgnore
    public Aircurrent getIncommingLongitudalAirflow() {
        return incommingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLongitudalAirflow() {
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getIncommingLatitudalAirflow() {
        return incommingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLatitudallAirflow() {
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    public void addIncommingMoisture(double incommingMoisture) {
        this.incommingMoisture = this.incommingMoisture + incommingMoisture;
    }

    public void processIncommingMoisture() {
        addAirMoisture(this.incommingMoisture);
        this.incommingMoisture = 0;
    }

    public void addAirMoisture(double extraAirmoisture) {
        if (extraAirmoisture > 0d) {
            if (extraAirmoisture + this.getAirMoisture() < 100d) {
                this.setAirMoisture(this.getAirMoisture() + extraAirmoisture);
            } else {
                this.setAirMoisture(100d);
            }
        }
    }

    public void reduceAirMoisture(double airmoistureReduction) {
        if (airmoistureReduction > 0d) {
            if (this.getAirMoisture() - airmoistureReduction > 0d) {
                this.setAirMoisture(this.getAirMoisture() - airmoistureReduction);
            } else {
                this.setAirMoisture(0d);
            }
        }
    }

    public void calculateNewMoistureLevel() {
        reduceAirMoisture(airMoistureLossage);
    }

    public void moveClouds() {
        double diffider = this.outgoingAircurrents.stream()
                .mapToInt(Aircurrent::getCurrentStrength)
                .sum();
        if (diffider != 0) {
            double transfer = this.getAirMoisture() / diffider;
            this.setAirMoisture(this.getAirMoisture() - (transfer * diffider));

            outgoingAircurrents.forEach(aircurrent -> aircurrent.transfer(transfer));
        }
    }

    public void addAirMoistureLossage(double heightAmount) {
        this.airMoistureLossage = this.airMoistureLossage + heightAmount;
    }

    @Override
    public String toString() {
        return "SkyTile{" +
                "id=" + id +
                ", airMoisture=" + airMoisture +
                ", airMoistureLossage=" + airMoistureLossage +
                ", outgoingAircurrents=" + outgoingAircurrents +
                ", incommingAircurrents=" + incommingAircurrents +
                '}';
    }

    public SkyTile createClone(Climate climeateClone) {
        SkyTile clone = new SkyTile();
        clone.setAirMoistureLossage(this.airMoistureLossage);
        clone.setAirMoisture(this.airMoisture);
        clone.setClimate(climeateClone);
        clone.setId(climeateClone.getId());
        getOutgoingAircurrents().forEach(aircurrent -> clone.getOutgoingAircurrents().add(aircurrent.createOutgoingClone(clone)));
        getIncommingAircurrents().forEach(aircurrent -> clone.getIncommingAircurrents().add(aircurrent.createIncommingClone(clone)));

        return clone;
    }

    public SkyTile createBareboneClone() {
        SkyTile clone = new SkyTile();
        clone.setId(this.getId());
        return clone;
    }
}
