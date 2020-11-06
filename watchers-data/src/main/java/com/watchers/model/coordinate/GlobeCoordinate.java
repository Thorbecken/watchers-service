package com.watchers.model.coordinate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.Continent;
import com.watchers.model.world.World;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@DiscriminatorValue(value = "GLOBE")
public class GlobeCoordinate extends Coordinate {

    protected GlobeCoordinate(long xCoord, long yCoord, World world, Continent continent) {
        super(xCoord, yCoord, CoordinateType.GLOBE, world, continent);
    }

    private GlobeCoordinate(){}

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

    @Override
    @JsonIgnore
    protected long getAdjustedYCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.UP && startincCoordinate == getWorld().getYSize()){
            return 1;  // set xcoord naar huidige waarde plus of minus de helft van ySize van de wereld. plus als xcoord groter of gelijk is aan de helft en minus als deze groter is.
        } else if(adjustment <= CoordinateHelper.DOWN && startincCoordinate == 1){
            return getWorld().getYSize(); // set xcoord naar huidige waarde plus of minus de helft van ySize van de wereld. plus als xcoord groter of gelijk is aan de helft en minus als deze groter is.
        } else {
            return startincCoordinate+adjustment;
        }
    }

    @Override
    @JsonIgnore
    public List<Coordinate> getNeighbours() {
        List<Coordinate> returnCoordinates = new ArrayList<>();
        returnCoordinates.add(super.getWorld().getCoordinate(getLeftCoordinate(), super.getYCoord()));
        returnCoordinates.add(super.getWorld().getCoordinate(getRightCoordinate(), super.getYCoord()));
        returnCoordinates.add(super.getWorld().getCoordinate(correctXByDown(super.getXCoord()), getDownCoordinate()));
        returnCoordinates.add(super.getWorld().getCoordinate(correctXByUp(super.getXCoord()), getUpCoordinate()));

        return returnCoordinates;
    }

    protected long correctXByDown(long xCoord) {
        if(getUpCoordinate() > getWorld().getYSize()) {
            long halfLenght = super.getWorld().getXSize()/2;
            return xCoord<halfLenght?xCoord+halfLenght:xCoord-halfLenght;
        } else {
            return xCoord;
        }
    }

    protected long correctXByUp(long xCoord) {
        if(getUpCoordinate() > getWorld().getYSize()) {
            long halfLenght = super.getWorld().getXSize()/2;
            return xCoord<halfLenght?xCoord+halfLenght:xCoord-halfLenght;
        } else {
            return xCoord;
        }
    }

    public Coordinate createBasicClone(World newWorld) {
        Coordinate clone = new GlobeCoordinate();
        clone.setId(getId());
        clone.setWorld(newWorld);
        clone.setXCoord(getXCoord());
        clone.setYCoord(getYCoord());
        clone.changeContinent(newWorld.getContinents().stream()
                .filter(oldContinent -> oldContinent.getId().equals(getContinent().getId()))
                .findFirst().get());

        return clone;
    }
}
