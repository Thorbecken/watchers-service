package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "aircurrent")
@SequenceGenerator(name = "AC_Gen", sequenceName = "AC_Seq", allocationSize = 1)
public class Aircurrent {

    @Id
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator = "AC_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "aircurrent_id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "incommingAircurrent_id", nullable = false)
    private IncommingAircurrent incommingAircurrent;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "outgoingAircurrent_id", nullable = false)
    private OutgoingAircurrent outgoingAircurrent;

    private int currentStrength;

    private AircurrentType aircurrentType;

    @JsonView(Views.Public.class)
    private long heightDifference;

    public Aircurrent(SkyTile endingSky, SkyTile startingSky, AircurrentType aircurrentType, int currentStrength) {
        this(endingSky.getRawIncommingAircurrents(), startingSky.getRawOutgoingAircurrents(), aircurrentType, currentStrength);
    }

    public Aircurrent(IncommingAircurrent incommingAircurrents, OutgoingAircurrent outgoingAircurrent, AircurrentType aircurrentType, int currentStrength) {
        this.incommingAircurrent = incommingAircurrents;
        this.outgoingAircurrent = outgoingAircurrent;
        this.aircurrentType = aircurrentType;
        this.currentStrength = currentStrength;

        this.getStartingSky().getOutgoingAircurrents().add(this);
        this.getEndingSky().getIncommingAircurrents().add(this);

        recalculateHeigthDifference();
    }

    public void transfer(double amountPerStrength) {
        double amount = amountPerStrength * currentStrength;
        double heightAmount = calculateHeightDifferenceEffect(amount);

        outgoingAircurrent.getStartingSky().addIncommingMoisture(amount);
        outgoingAircurrent.getStartingSky().addAirMoistureLossage(heightAmount);
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

    public Aircurrent createOutgoingClone(SkyTile skyClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(skyClone.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setOutgoingAircurrent(skyClone.getRawOutgoingAircurrents());
        clone.setIncommingAircurrent(skyClone.getRawIncommingAircurrents());
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    public Aircurrent createIncommingClone(SkyTile skyClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(skyClone.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setOutgoingAircurrent(skyClone.getRawOutgoingAircurrents());
        clone.setIncommingAircurrent(skyClone.getRawIncommingAircurrents());
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    @Override
    public String toString() {
        return "Aircurrent{" +
                "id =" + id +
                ", heightDifference =" + heightDifference +
                ", direction = " + aircurrentType +
                '}';
    }

    public void recalculateHeigthDifference() {
        long startingHeight = incommingAircurrent.getEndingSky().getClimate().getCoordinate().getTile().getHeight();
        long endingHeight = outgoingAircurrent.getStartingSky().getClimate().getCoordinate().getTile().getHeight();

        this.heightDifference = startingHeight - endingHeight;
    }

    public SkyTile getEndingSky() {
        return this.incommingAircurrent.getEndingSky();
    }

    public void setEndingSky(SkyTile endingSky) {
        this.incommingAircurrent.setEndingSky(endingSky);
    }

    public SkyTile getStartingSky() {
        return this.outgoingAircurrent.getStartingSky();
    }

    public void setStartingSky(SkyTile startingSky) {
        this.outgoingAircurrent.setStartingSky(startingSky);
    }
}
