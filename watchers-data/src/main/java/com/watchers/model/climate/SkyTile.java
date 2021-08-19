package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonView(Views.Public.class)
    double airMoisture;

    @Transient
    double incommingMoisture;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Climate climate;

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private double airMoistureLossage;

    @JsonView(Views.Public.class)
    @OneToOne(mappedBy = "endingSky", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private IncommingAircurrent incommingAircurrents = new IncommingAircurrent(this);

    @JsonView(Views.Public.class)
    @OneToOne(mappedBy = "startingSky", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private OutgoingAircurrent outgoingAircurrents = new OutgoingAircurrent(this);

    public SkyTile(Climate climate) {
        this.climate = climate;
    }

    @JsonIgnore
    public Aircurrent getIncommingLongitudalAirflow() {
        return incommingAircurrents.getIncommingAircurrents().stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLongitudalAirflow() {
        return outgoingAircurrents.getOutgoingAircurrent().stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getIncommingLatitudalAirflow() {
        return incommingAircurrents.getIncommingAircurrents().stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLatitudallAirflow() {
        return outgoingAircurrents.getOutgoingAircurrent().stream().filter(aircurrent -> AircurrentType
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
        if (extraAirmoisture > 0L) {
            if (extraAirmoisture + this.airMoisture < 100L) {
                this.airMoisture = this.airMoisture + extraAirmoisture;
            } else {
                this.airMoisture = 100L;
            }
        }
    }

    public void reduceAirMoisture(double airmoistureReduction) {
        if (airmoistureReduction > 0L) {
            if (this.airMoisture - airmoistureReduction > 0L) {
                this.airMoisture = this.airMoisture - airmoistureReduction;
            } else {
                this.airMoisture = 0L;
            }
        }
    }

    public void calculateNewMoistureLevel() {
        reduceAirMoisture(airMoistureLossage);
    }

    public void moveClouds() {
        List<Aircurrent> outgoingAircurrents = this.outgoingAircurrents.getOutgoingAircurrent();
        double diffider = outgoingAircurrents.stream()
                .mapToInt(Aircurrent::getCurrentStrength)
                .sum();
        double transfer = airMoisture / diffider;
        this.airMoisture = this.airMoisture - (transfer * diffider);

        outgoingAircurrents.forEach(aircurrent -> aircurrent.transfer(transfer));
    }

    public void addAirMoistureLossage(double heightAmount) {
        this.airMoistureLossage = this.airMoistureLossage + heightAmount;
    }

    public OutgoingAircurrent getRawOutgoingAircurrents() {
        return this.outgoingAircurrents;
    }

    public List<Aircurrent> getOutgoingAircurrents() {
        return this.outgoingAircurrents.getOutgoingAircurrent();
    }

    public void setRawOutgoingAircurrents(OutgoingAircurrent outgoingAircurrent) {
        this.outgoingAircurrents = outgoingAircurrent;
    }

    public IncommingAircurrent getRawIncommingAircurrents() {
        return this.incommingAircurrents;
    }

    public List<Aircurrent> getIncommingAircurrents() {
        return this.incommingAircurrents.getIncommingAircurrents();
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
}
