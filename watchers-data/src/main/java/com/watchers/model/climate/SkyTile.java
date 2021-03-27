package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "sky")
@SequenceGenerator(name="Sky_Gen", sequenceName="Sky_Seq", allocationSize = 1)
public class SkyTile {

    @Id
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator="Sky_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "sky_id", nullable = false)
    private Long id;

    @JsonView(Views.Public.class)
    long airMoisture;

    @Transient
    long incommingMoisture;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private Climate climate;

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private long airMoistureLossage;

    @JsonView(Views.Public.class)
    @OneToMany(mappedBy = "endingSky", fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private List<Aircurrent> incommingAircurrents = new ArrayList<>(2);

    @JsonView(Views.Public.class)
    @OneToMany(mappedBy = "startingSky", fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private List<Aircurrent> outgoingAircurrents = new ArrayList<>(2);

    public SkyTile(Climate climate){
        this.climate = climate;
    }

    public Aircurrent getIncommingLongitudalAirflow(){
        return incommingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    public Aircurrent getOutgoingLongitudalAirflow(){
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    public Aircurrent getIncommingLatitudalAirflow(){
        return incommingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    public Aircurrent getOutgoingLatitudallAirflow(){
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    public void addIncommingMoisture(long incommingMoisture){
        this.incommingMoisture =+ incommingMoisture;
    }

    public void processIncommingMoisture(){
        addAirMoisture(this.incommingMoisture);
        this.incommingMoisture = 0;
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

    public void calculateNewMoistureLevel() {
        reduceAirMoisture(airMoistureLossage);
    }

    public void moveClouds() {
        List<Aircurrent> outgoingAircurrents = this.outgoingAircurrents;
        int totalUpstreamSkies = outgoingAircurrents.size();
        long transfer = airMoisture/totalUpstreamSkies;
        this.airMoisture = this.airMoisture-(transfer*totalUpstreamSkies);

        outgoingAircurrents.forEach(aircurrent -> aircurrent.transfer(transfer));
    }

    public void addAirMoistureLossage(long heightAmount) {
        this.airMoistureLossage =+ heightAmount;
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
        clone.setId(this.id);
        outgoingAircurrents.forEach(aircurrent -> clone.getOutgoingAircurrents().add(aircurrent.createOutgoingClone(clone)));
        incommingAircurrents.forEach(aircurrent -> clone.getIncommingAircurrents().add(aircurrent.createIncommingClone(clone)));

        return clone;
    }
}
