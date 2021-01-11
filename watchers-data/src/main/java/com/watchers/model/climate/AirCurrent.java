package com.watchers.model.climate;

import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data
public class AirCurrent {

    private int yCoordinate;
    private int airCurrentSize;
    private List<Climate> airCurrentClimates;

    public AirCurrent(List<Climate> climateList){
        this.airCurrentClimates = climateList;
        this.airCurrentClimates.sort(Comparator.comparing(Climate::getLongitude));
        this.airCurrentSize = climateList.size();
        this.yCoordinate = ((int) climateList.get(0).getCoordinate().getYCoord());
    }

    private void setAirCurrentClimates(List<Climate> climateList, int yCoordinate) {
        this.airCurrentClimates = climateList;
    }

    private void setAirCurrentSize(int airCurrentSize) {
        this.airCurrentSize = airCurrentSize;
    }

    private void setyCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public void moveClouds(){
        // sets the first incoming cloud from the last in the array.
        airCurrentClimates.get(0)
                .setIncomingCloud(airCurrentClimates.get(airCurrentSize -1).getCurrentCloud());

        // transfers the rest of the clouds.
        for (int currentX = 1; currentX < airCurrentSize; currentX++) {
            int previousX = currentX-1;
            airCurrentClimates.get(currentX)
                    .setIncomingCloud(airCurrentClimates.get(previousX).getCurrentCloud());
        }

        airCurrentClimates.forEach(climate -> climate.setCurrentCloud(climate.getIncomingCloud()));
        //airCurrentClimates.forEach(climate -> climate.getCurrentCloud().setCurrentClimate(climate));
    }
}
