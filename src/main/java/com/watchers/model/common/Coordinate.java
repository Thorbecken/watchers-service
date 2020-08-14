package com.watchers.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.World;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Entity
@Table(name = "coordinate")
@NoArgsConstructor
@SequenceGenerator(name="Coordinate_Gen", sequenceName="Coordinate_Seq", allocationSize = 1)
public class Coordinate {

    @Id
    @JsonIgnore
    @GeneratedValue(generator="Coordinate_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "coordinate_id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonProperty("xCoord")
    @Column(name = "xCoord")
    private long xCoord;

    @JsonProperty("yCoord")
    @Column(name = "yCoord")
    private long yCoord;

    public Coordinate(long xCoord, long yCoord, World world) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.world = world;
    }

    public Coordinate(long xCoord, long yCoord, Coordinate baseCoordinate) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.world = baseCoordinate.getWorld();
    }

    @JsonIgnore
    public List<Coordinate> getNeighbours() {
        boolean down = yCoord > 1L;
        boolean up = yCoord < this.world.getYSize();

        List<Coordinate> returnCoordinates = new ArrayList<>();
        returnCoordinates.add(new Coordinate(getLeftCoordinate(), yCoord, world));
        returnCoordinates.add(new Coordinate(getRightCoordinate(), yCoord, world));


        if(down) {
            Coordinate downCoordinate = new Coordinate(xCoord, yCoord - 1, world);
            returnCoordinates.add(downCoordinate);
        }
        if(up) {
            Coordinate upCoordinate = new Coordinate(xCoord, yCoord + 1, world);
            returnCoordinates.add(upCoordinate);
        }

        return returnCoordinates;
    }

    @JsonIgnore
    private long getRightCoordinate() {
        return getAdjustedXCoordinate(CoordinateHelper.RIGHT, this.xCoord);
    }

    @JsonIgnore
    private long getLeftCoordinate() {
        return getAdjustedXCoordinate(CoordinateHelper.LEFT, this.xCoord);
    }

    @JsonIgnore
    private long getUpCoordinate() {
        return getAdjustedYCoordinate(CoordinateHelper.UP, this.yCoord);
    }

    @JsonIgnore
    private long getDownCoordinate() {
        return getAdjustedYCoordinate(CoordinateHelper.DOWN, this.yCoord);
    }

    @JsonIgnore
    public long getXCoordinateFromTile(long distance){
        return getXCoordinateFromTile(distance, this.getXCoord());
    }

    @JsonIgnore
    private long getXCoordinateFromTile(long distance, long startingCoordinate){
        if(distance <= CoordinateHelper.LEFT){
            return getXCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedXCoordinate(CoordinateHelper.LEFT, startingCoordinate));
        } else if(distance >= CoordinateHelper.RIGHT){
            return getXCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedXCoordinate(CoordinateHelper.RIGHT, startingCoordinate));
        } else {
            return startingCoordinate;
        }
    }

    @JsonIgnore
    public long getYCoordinateFromTile(long distance){
        return getYCoordinateFromTile(distance, this.getYCoord());
    }

    @JsonIgnore
    private long getYCoordinateFromTile(long distance, long startingCoordinate){
        if(distance <= CoordinateHelper.DOWN){
            return getYCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedYCoordinate(CoordinateHelper.DOWN, startingCoordinate));
        } else if(distance >= CoordinateHelper.UP){
            return getYCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedYCoordinate(CoordinateHelper.UP, startingCoordinate));
        } else {
            return startingCoordinate;
        }
    }

    public Coordinate calculateDistantCoordinate(int xVelocity, int yVelocity) {
        long newX = this.getXCoordinateFromTile(xVelocity);
        long newY = this.getYCoordinateFromTile(yVelocity);
        return new Coordinate(newX, newY, this);
    }

    private long decreaseDistanceToZero(long distance){
        if(distance < 0){
            return distance+CoordinateHelper.RIGHT;
        } else if(distance > 0){
            return distance+CoordinateHelper.LEFT;
        } else {
            return 0;
        }
    }

    @JsonIgnore
    private long getAdjustedXCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.RIGHT && startincCoordinate == this.world.getXSize()){
            return 1;
        } else if(adjustment <= CoordinateHelper.LEFT && startincCoordinate == 1){
            return this.world.getXSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    @JsonIgnore
    private long getAdjustedYCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.UP && startincCoordinate == this.world.getYSize()){
            return 1;
        } else if(adjustment <= CoordinateHelper.DOWN && startincCoordinate == 1){
            return this.world.getYSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    @JsonIgnore
    public List<Coordinate> getCoordinatesWithinRange(int range) {
        return getCoordinatesWithinRange(Collections.singletonList(this), range);
    }

    @JsonIgnore
    private List<Coordinate> getCoordinatesWithinRange(List<Coordinate> coordinates, int range) {
        if(range>=1) {
            List<Coordinate> returnList = new ArrayList<>();
            coordinates.forEach(
                    coordinate -> returnList.addAll(coordinate.getNeighbours())
            );

            return getCoordinatesWithinRange(returnList, coordinates, range-1);
        } else {
            return coordinates;
        }
    }

    @JsonIgnore
    private List<Coordinate> getCoordinatesWithinRange(List<Coordinate> coordinates, List<Coordinate> oldCoordinates, int range) {
        if(range>=1) {
            List<Coordinate> returnList = new ArrayList<>();
            coordinates.forEach(
                    coordinate -> {
                        if(!oldCoordinates.contains(coordinate)){
                            returnList.addAll(coordinate.getNeighbours());
                        }
                    }
            );

            return getCoordinatesWithinRange(returnList, coordinates, range-1);
        } else {
            return coordinates;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) o;
        return xCoord == that.xCoord &&
                yCoord == that.yCoord;
    }

    @Override
    public int hashCode() {
        String string = xCoord + "," + yCoord;
        return string.hashCode();
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                '}';
    }
}
