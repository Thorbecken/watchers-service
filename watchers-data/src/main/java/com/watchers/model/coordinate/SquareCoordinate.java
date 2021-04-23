package com.watchers.model.coordinate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;

import java.util.ArrayList;
import java.util.List;

public class SquareCoordinate extends Coordinate {
    public SquareCoordinate(long xCoord, long yCoord, World world, Continent continent) {
        super(xCoord, yCoord, CoordinateType.SQUARE, world, continent);
    }

    private SquareCoordinate(){}

    @Override
    @JsonIgnore
    protected long getAdjustedXCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.RIGHT && startincCoordinate == getWorld().getXSize()){
            return 1;
        } else if(adjustment <= CoordinateHelper.LEFT && startincCoordinate == 1){
            return 1;
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
            return 1;
        } else {
            return startincCoordinate+adjustment;
        }
    }

    public Coordinate createBasicClone(World newWorld) {
        Coordinate clone = new SquareCoordinate();
        clone.setId(getId());
        clone.setWorld(newWorld);
        clone.setXCoord(getXCoord());
        clone.setYCoord(getYCoord());
        clone.changeContinent(newWorld.getContinents().stream()
                .filter(oldContinent -> oldContinent.getId().equals(getContinent().getId()))
                .findFirst().orElseThrow());
        clone.setTile(this.getTile().createClone(clone));
        clone.setClimate(this.getClimate().createClone(clone));

        return clone;
    }
}
