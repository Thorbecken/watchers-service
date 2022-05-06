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

    @JsonView(Views.Public.class)
    @Column(name = "starting_x_coordinate", nullable = false)
    private Long startingXCoordinate;

    @JsonView(Views.Public.class)
    @Column(name = "ending_x_coordinate", nullable = false)
    private Long endingXCoordinate;

    @JsonView(Views.Public.class)
    @Column(name = "starting_y_coordinate", nullable = false)
    private Long startingYCoordinate;

    @JsonView(Views.Public.class)
    @Column(name = "ending_y_coordinate", nullable = false)
    private Long endingYCoordinate;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private SkyTile endingSky;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private SkyTile startingSky;

    @JsonView(Views.Public.class)
    private int currentStrength;

    @JsonView(Views.Public.class)
    private AircurrentType aircurrentType;

    @JsonView(Views.Public.class)
    private long heightDifference;

    public Aircurrent(SkyTile startingSky, SkyTile endingSky, AircurrentType aircurrentType, int currentStrength) {
        this.aircurrentType = aircurrentType;
        this.currentStrength = currentStrength;

        this.startingSky = startingSky;
        this.endingSky = endingSky;

        startingSky.getOutgoingAircurrents().add(this);
        endingSky.getIncommingAircurrents().add(this);

        recalculateHeigthDifference();

        this.startingXCoordinate = startingSky.getClimate().getCoordinate().getXCoord();
        this.endingXCoordinate = endingSky.getClimate().getCoordinate().getXCoord();
        this.startingYCoordinate = startingSky.getClimate().getCoordinate().getYCoord();
        this.endingYCoordinate = endingSky.getClimate().getCoordinate().getYCoord();
    }

    public void resetCoordinates(){
        this.startingXCoordinate = this.getStartingSky().getClimate().getCoordinate().getXCoord();
        this.endingXCoordinate = this.getEndingSky().getClimate().getCoordinate().getXCoord();
        this.startingYCoordinate = this.getStartingSky().getClimate().getCoordinate().getYCoord();
        this.endingYCoordinate = this.getEndingSky().getClimate().getCoordinate().getYCoord();
    }

    public void transfer(double amountPerStrength) {
        double amount = amountPerStrength * currentStrength;
        double heightAmount = calculateHeightDifferenceEffect(amount);

        endingSky.addIncommingMoisture(amount);
        startingSky.addAirMoistureLossage(heightAmount);
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
        clone.setStartingSky(skyClone);
        clone.setEndingSky(this.endingSky);
        clone.setHeightDifference(this.heightDifference);
        return clone;
    }

    public Aircurrent createIncommingClone(SkyTile skyClone) {
        Aircurrent clone = new Aircurrent();
        clone.setId(this.getId());
        clone.setAircurrentType(this.aircurrentType);
        clone.setCurrentStrength(this.currentStrength);
        clone.setEndingSky(skyClone);
        clone.setStartingSky(this.startingSky);
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
