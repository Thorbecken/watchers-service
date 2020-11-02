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

@Data
@Entity
@DiscriminatorValue(value = "GLOBE")
public class GlobeCoordinate extends Coordinate {


    @JsonProperty("northernHemisphere")
    @Column(name = "northernHemisphere")
    private boolean northernHemisphere;

    protected GlobeCoordinate(long xCoord, long yCoord, boolean northernHemisphere, World world, Continent continent) {
        super(xCoord, yCoord, CoordinateType.GLOBE, world, continent);
        this.northernHemisphere = northernHemisphere;
    }

    private GlobeCoordinate(){}

    @Override
    @JsonIgnore
    protected long getAdjustedXCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.RIGHT && startincCoordinate == getWorld().getXSize()){
            northernHemisphere = !northernHemisphere;
            return 1;
        } else if(adjustment <= CoordinateHelper.LEFT && startincCoordinate == 1){
            northernHemisphere = !northernHemisphere;
            return getWorld().getXSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    @Override
    @JsonIgnore
    protected long getAdjustedYCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.UP && startincCoordinate == getWorld().getYSize()){
            northernHemisphere = !northernHemisphere;
            return 1;
        } else if(adjustment <= CoordinateHelper.DOWN && startincCoordinate == 1){
            northernHemisphere = !northernHemisphere;
            return getWorld().getYSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    public Coordinate createBasicClone(World newWorld) {
        Coordinate clone = new GlobeCoordinate();
        clone.setId(getId());
        clone.setWorld(newWorld);
        clone.setXCoord(getXCoord());
        clone.setYCoord(getYCoord());
        clone.setContinent(newWorld.getContinents().stream()
                .filter(oldContinent -> oldContinent.getId().equals(getContinent().getId()))
                .findFirst().get());

        return clone;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobeCoordinate)) return false;
        if (!super.equals(o)) return false;

        GlobeCoordinate that = (GlobeCoordinate) o;

        return northernHemisphere == that.northernHemisphere;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (northernHemisphere ? 1 : 0);
        return result;
    }
}
