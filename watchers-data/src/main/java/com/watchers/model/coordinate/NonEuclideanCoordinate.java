package com.watchers.model.coordinate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.climate.Climate;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue(value = "NON_EUCLIDEAN")
public class NonEuclideanCoordinate extends Coordinate {

    protected NonEuclideanCoordinate(long xCoord, long yCoord, World world, Continent continent) {
        super(xCoord, yCoord, CoordinateType.NON_EUCLIDEAN, world, continent);

    }

    private NonEuclideanCoordinate(){}

    @Override
    @JsonIgnore
    protected long getAdjustedXCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.RIGHT && startincCoordinate == getWorld().getXSize()){
            return 1;
        } else if(adjustment <= CoordinateHelper.LEFT && startincCoordinate == 1){
            return getWorld().getXSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    @JsonIgnore
    public List<Coordinate> getNeighbours() {
        List<Coordinate> returnCoordinates = new ArrayList<>();
        returnCoordinates.add(super.getWorld().getCoordinate(getLeftCoordinate(), super.getYCoord()));
        returnCoordinates.add(super.getWorld().getCoordinate(getRightCoordinate(), super.getYCoord()));
        returnCoordinates.add(super.getWorld().getCoordinate(super.getXCoord(), getDownCoordinate()));
        returnCoordinates.add(super.getWorld().getCoordinate(super.getXCoord(), getUpCoordinate()));

        return returnCoordinates;
    }

    @Override
    @JsonIgnore
    protected long getAdjustedYCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.UP && startincCoordinate == getWorld().getYSize()){
            return 1;
        } else if(adjustment <= CoordinateHelper.DOWN && startincCoordinate == 1){
            return getWorld().getYSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    public Coordinate createClone(World newWorld) {
        Coordinate clone = new NonEuclideanCoordinate();
        clone.setId(getId());
        clone.setWorld(newWorld);
        clone.setXCoord(getXCoord());
        clone.setYCoord(getYCoord());
        clone.setCoordinateType(getCoordinateType());
        clone.changeContinent(newWorld.getContinents().stream()
                .filter(oldContinent -> oldContinent.getId().equals(getContinent().getId()))
                .findFirst().get());
        clone.setTile(this.getTile().createClone(clone));
        Climate climate = this.getClimate();
        Climate climateClone = climate.createClone(clone);
        clone.setClimate(climateClone);

        clone.getActors().addAll(
                this.getActors().stream()
                        .map(actor -> actor.createClone(clone))
                        .collect(Collectors.toSet())
        );

        if(this.getPointOfInterest() != null){
            clone.setPointOfInterest(this.getPointOfInterest().createClone(clone, null));
        }

        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NonEuclideanCoordinate)) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public double getDistance(Coordinate coordinate) {
        return Math.abs(this.getAdjustedXDistance(coordinate)) + Math.abs(this.getAdjustedYDistance(coordinate));
    }

    @Override
    public long getAdjustedXDistance(Coordinate coordinate) {
        //10
        long size = this.getWorld().getXSize();
        // 5
        long halfSize = size/2;
        // 6
        long rawDifference = this.getXCoord() - coordinate.getXCoord();
        if(rawDifference > 0){
            if(rawDifference <= halfSize) {
                return rawDifference;
            } else {
                // 6-10 = -4
                return rawDifference - size;
            }
        } else {
            // -6 <= -5
            if(rawDifference <= -halfSize) {
                return rawDifference;
            } else {
                // -6+10 = 4
                return rawDifference + size;
            }
        }
    }

    @Override
    public long getAdjustedYDistance(Coordinate coordinate) {
        //10
        long size = this.getWorld().getYSize();
        // 5
        long halfSize = size/2;
        // 6
        long rawDifference = this.getYCoord() - coordinate.getYCoord();
        if(rawDifference > 0){
            if(rawDifference <= halfSize) {
                return rawDifference;
            } else {
                // 6-10 = -4
                return rawDifference - size;
            }
        } else {
            // -6 <= -5
            if(rawDifference <= -halfSize) {
                return rawDifference;
            } else {
                // -6+10 = 4
                return rawDifference + size;
            }
        }
    }
}
