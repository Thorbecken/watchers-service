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
@SequenceGenerator(name="AC_Gen", sequenceName="AC_Seq", allocationSize = 1)
public class Aircurrent {

    @Id
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator="AC_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "aircurrent_id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private SkyTile startingSky;

    @JsonIgnore
    @ManyToOne
    private SkyTile endingSky;

    private int currentStrength;

    private AircurrentType aircurrentType;

    @JsonView(Views.Public.class)
    private long heightDifference;

    public Aircurrent(SkyTile startingSky, SkyTile endingSky, AircurrentType aircurrentType, int currentStrength){
        this.startingSky = startingSky;
        this.endingSky = endingSky;
        this.aircurrentType = aircurrentType;
        this.currentStrength = currentStrength;

        recalculateHeigthDifference();
    }

    public void transfer(long amountPerStrength){
        long amount = amountPerStrength*currentStrength;
        long heightAmount = calculateHeightDifferenceEffect(amount);

        endingSky.addIncommingMoisture(amount);
        endingSky.addAirMoistureLossage(heightAmount);
    }

    public long calculateHeightDifferenceEffect(long airMoisture) {
        if(airMoisture > 0L && heightDifference > 0L) {
            if(airMoisture>heightDifference) {
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
        clone.setId(this.id);
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setStartingSky(skyClone);

        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    public Aircurrent createIncommingClone(SkyTile skyClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(this.id);
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setEndingSky(skyClone);
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    @Override
    public String toString() {
        return "Aircurrent{" +
                "id=" + id +
                ", heightDifference=" + heightDifference +
                '}';
    }

    public void recalculateHeigthDifference() {
        long startingHeight = startingSky.getClimate().getCoordinate().getTile().getHeight();
        long endingHeight = endingSky.getClimate().getCoordinate().getTile().getHeight();

        this.heightDifference = startingHeight - endingHeight;
    }
}
