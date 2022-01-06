package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incommingAircurrent_id", nullable = false)
    private IncommingAircurrent incommingAircurrent;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outgoingAircurrent_id", nullable = false)
    private OutgoingAircurrent outgoingAircurrent;

    private int currentStrength;

    private AircurrentType aircurrentType;

    @JsonView(Views.Public.class)
    private long heightDifference;

    public Aircurrent(SkyTile startingSky, SkyTile endingSky, AircurrentType aircurrentType, int currentStrength) {
        this.incommingAircurrent = endingSky.getRawIncommingAircurrents();
        this.outgoingAircurrent = startingSky.getRawOutgoingAircurrents();
        this.aircurrentType = aircurrentType;
        this.currentStrength = currentStrength;

        this.getStartingSky().getRawOutgoingAircurrents().add(this);
        this.getEndingSky().getRawIncommingAircurrents().add(this);

        recalculateHeigthDifference();
    }

    public void transfer(double amountPerStrength) {
        double amount = amountPerStrength * currentStrength;
        double heightAmount = calculateHeightDifferenceEffect(amount);

        incommingAircurrent.getEndingSky().addIncommingMoisture(amount);
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
        clone.setId(this.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setOutgoingAircurrent(skyClone.getRawOutgoingAircurrents());
        clone.setIncommingAircurrent(incommingAircurrent.createClone());
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    public Aircurrent createIncommingClone(SkyTile skyClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(this.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setOutgoingAircurrent(outgoingAircurrent.createClone());
        clone.setIncommingAircurrent(skyClone.getRawIncommingAircurrents());
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    public void recalculateHeigthDifference() {
        SkyTile endingSky = this.getEndingSky();
        Climate endingClimate = endingSky.getClimate();
        Coordinate endingCoordinate = endingClimate.getCoordinate();
        Tile endingTile = endingCoordinate.getTile();
        long endingHeight = endingTile.getHeight();

        SkyTile startingSky = this.getStartingSky();
        Climate startingClimate = startingSky.getClimate();
        Coordinate startingCoordinate = startingClimate.getCoordinate();
        Tile startingTile = startingCoordinate.getTile();
        long startingHeight = startingTile.getHeight();

        this.heightDifference = endingHeight - startingHeight;
    }

    @JsonIgnore
    public SkyTile getEndingSky() {
        return this.incommingAircurrent.getEndingSky();
    }

    public void setEndingSky(SkyTile endingSky) {
        this.incommingAircurrent.setEndingSky(endingSky);
    }

    @JsonIgnore
    public SkyTile getStartingSky() {
        return this.outgoingAircurrent.getStartingSky();
    }

    public void setStartingSky(SkyTile startingSky) {
        this.outgoingAircurrent.setStartingSky(startingSky);
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
}
