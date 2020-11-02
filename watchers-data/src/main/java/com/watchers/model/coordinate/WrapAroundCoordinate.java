package com.watchers.model.coordinate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.environment.Continent;
import com.watchers.model.world.World;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "WRAP_AROUND")
public class WrapAroundCoordinate extends Coordinate {

    protected WrapAroundCoordinate(long xCoord, long yCoord, World world, Continent continent) {
        super(xCoord, yCoord, CoordinateType.WRAP_AROUND, world, continent);
    }

    private WrapAroundCoordinate(){}

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
            return getWorld().getYSize();
        } else if(adjustment <= CoordinateHelper.DOWN && startincCoordinate == 1){
            return 1;
        } else {
            return startincCoordinate+adjustment;
        }
    }

    public Coordinate createBasicClone(World newWorld) {
        Coordinate clone = new WrapAroundCoordinate();
        clone.setId(getId());
        clone.setWorld(newWorld);
        clone.setXCoord(getXCoord());
        clone.setYCoord(getYCoord());
        clone.setContinent(newWorld.getContinents().stream()
                .filter(oldContinent -> oldContinent.getId().equals(getContinent().getId()))
                .findFirst().get());

        return clone;
    }
}
