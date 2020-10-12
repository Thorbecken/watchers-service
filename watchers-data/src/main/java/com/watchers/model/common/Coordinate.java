package com.watchers.model.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.Tile;
import com.watchers.model.environment.World;
import com.watchers.model.actor.Actor;
import com.watchers.model.environment.Continent;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonProperty("xCoord")
    @Column(name = "xCoord")
    private long xCoord;

    @JsonProperty("yCoord")
    @Column(name = "yCoord")
    private long yCoord;

    @JsonProperty("tile")
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade=CascadeType.ALL, orphanRemoval = true)
    private Tile tile;

    @JsonProperty("actors")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade=CascadeType.ALL, orphanRemoval = true)
    private Set<Actor> actors = new HashSet<>();

    @JsonIgnoreProperties({"world", "coordinates", "type" })
    @ManyToOne(fetch = FetchType.EAGER)
    private Continent continent;

    public Coordinate(long xCoord, long yCoord, World world, Continent continent) {
        this.yCoord = yCoord;
        this.xCoord = xCoord;
        this.world = world;
        this.continent = continent;

        this.tile = new Tile(this, continent);
    }

    public void changeContinent(Continent newContinent){
        if(continent != null) {
            continent.removeCoordinate(this);
        }
        newContinent.addCoordinate(this);
    }

    @JsonIgnore
    public List<Coordinate> getNeighbours() {
        List<Coordinate> returnCoordinates = new ArrayList<>();
        returnCoordinates.add(world.getCoordinate(getLeftCoordinate(), yCoord));
        returnCoordinates.add(world.getCoordinate(getRightCoordinate(), yCoord));
        returnCoordinates.add(world.getCoordinate(xCoord, getDownCoordinate()));
        returnCoordinates.add(world.getCoordinate(xCoord, getUpCoordinate()));

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
        return world.getCoordinate(newX, newY);
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
    @SuppressWarnings("unused")
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

    public Coordinate createBasicClone(World newWorld) {
        Coordinate clone = new Coordinate();
        clone.setId(this.id);
        clone.setWorld(newWorld);
        clone.setXCoord(this.xCoord);
        clone.setYCoord(this.yCoord);
        clone.setContinent(newWorld.getContinents().stream()
                .filter(oldContinent -> oldContinent.getId().equals(this.continent.getId()))
                .findFirst().get());

        return clone;
    }
}
