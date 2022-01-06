package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

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
    @OneToOne(mappedBy = "startingSky", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private OutgoingAircurrent outgoingAircurrents = new OutgoingAircurrent(this);

    @JsonView(Views.Public.class)
    @OneToOne(mappedBy = "endingSky", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private IncommingAircurrent incommingAircurrents = new IncommingAircurrent(this);

    public SkyTile(Climate climate) {
        this.climate = climate;
    }

    @JsonIgnore
    public Aircurrent getIncommingLongitudalAirflow() {
        List<Aircurrent> incommingAircurrent = incommingAircurrents.getAircurrentList();
        return incommingAircurrent.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLongitudalAirflow() {
        List<Aircurrent> outgoingAircurrent = outgoingAircurrents.getAircurrentList();
        return outgoingAircurrent.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getIncommingLatitudalAirflow() {
        List<Aircurrent> incommingAircurrent = incommingAircurrents.getAircurrentList();
        return incommingAircurrent.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLatitudallAirflow() {
        List<Aircurrent> outgoingAircurrent = outgoingAircurrents.getAircurrentList();
        return outgoingAircurrent.stream().filter(aircurrent -> AircurrentType
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
        List<Aircurrent> outgoingAircurrents = this.outgoingAircurrents.getAircurrentList();
        double diffider = outgoingAircurrents.stream()
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

    @JsonIgnore
    public OutgoingAircurrent getRawOutgoingAircurrents() {
        return this.outgoingAircurrents;
    }

    @JsonIgnore
    public List<Aircurrent> getOutgoingAircurrents() {
        return this.outgoingAircurrents.getAircurrentList();
    }

    public void setRawOutgoingAircurrents(OutgoingAircurrent outgoingAircurrent) {
        this.outgoingAircurrents = outgoingAircurrent;
    }

    @JsonIgnore
    public IncommingAircurrent getRawIncommingAircurrents() {
        return this.incommingAircurrents;
    }

    @JsonIgnore
    public List<Aircurrent> getIncommingAircurrents() {
        return this.incommingAircurrents.getAircurrentList();
    }

    public void setRawIncommingAircurrents(IncommingAircurrent incommingAircurrent) {
        this.incommingAircurrents = incommingAircurrent;
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
        clone.getRawOutgoingAircurrents().setId(climeateClone.getId());
        clone.getRawOutgoingAircurrents().setStartingSky(clone);
        clone.getRawIncommingAircurrents().setId(climeateClone.getId());
        clone.getRawIncommingAircurrents().setEndingSky(clone);
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
