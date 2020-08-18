package com.watchers.components.continentaldrift;

import com.watchers.model.environment.Continent;
import com.watchers.model.environment.World;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ContinentalDriftDirectionChanger {

    private int driftVelocity;
    private int drifFlux;

    public void assignFirstOrNewDriftDirections(World world){
        world.getContinents().forEach(continent -> continent.assignNewDriftDirection(driftVelocity, world));
    }

    public ContinentalDriftDirectionChanger(@Value("${watch.driftVelocity}") int driftVelocity, @Value("${watch.driftFlux}") int drifFlux) {
        this.driftVelocity = driftVelocity;
        this.drifFlux = drifFlux;
    }

    public World process(World world){
        new ContinentalDriftDirectionMethodObject(false,  world.getLastContinentInFlux())
                .adjustContinentelDriftFlux(world, drifFlux, driftVelocity);

        return world;
    }

    class ContinentalDriftDirectionMethodObject {
        private boolean lastChangedContinentFound;
        private int currentDriftChanges;
        private long lastChangedContinentelDrift;

        ContinentalDriftDirectionMethodObject(boolean lastChangedContinentFound, long lastChangedContinentelDrift){
            this.lastChangedContinentFound = lastChangedContinentFound;
            this.currentDriftChanges = 0;
            this.lastChangedContinentelDrift = lastChangedContinentelDrift;
        }


        void adjustContinentelDriftFlux(World world, int drifFlux, int driftVelocity) {
            List<Continent> continents = new ArrayList<>(world.getContinents());
            continents.sort(Comparator.comparing(Continent::getId));

            if(drifFlux >= continents.size()){
                //Just a speed hack, does the same as below but without a lot of looping around.
                continents.forEach(continent -> continent.assignNewDriftDirection(driftVelocity, world));
            } else {
                changeContinentalDriftDirections(continents, drifFlux, driftVelocity, world);
            }
        }

        /**
         * Loops trough the continents till the assigned number of continents have changed their course.
         * @param continents List of the continents of the world
         * @param drifFlux the amount of continents that will change their course
         * @param driftVelocity the max direction the continent can have on the X or Y axis.
         * @param world the world in which the continent exists
         */
        private void changeContinentalDriftDirections(List<Continent> continents, int drifFlux, int driftVelocity, World world){
            for (long i = 0; i < continents.size() ; i++) {
                Continent currentContinent = continents.get((int)i);

                searchAndChangeDirection(currentContinent, driftVelocity, world);

                if(currentDriftChanges == drifFlux){
                    break;
                } else if (i == continents.size()-1 && currentDriftChanges < drifFlux){
                    changeContinentalDriftDirections(continents, drifFlux, driftVelocity, world);
                    break;
                }
            }
        }

        /**
         * Saveguard methode to change continentals drift directions in order.
         * This method also keeps track of the number of continents that have changed the direction.
         * @param currentContinent the continent that has focus of the loop
         * @param driftVelocity the max speed on the X or Y axis direction
         */
        private void searchAndChangeDirection(Continent currentContinent, int driftVelocity, World world) {
            if(!lastChangedContinentFound && currentContinent.getId() == lastChangedContinentelDrift){
                lastChangedContinentFound = true;
            } else if(lastChangedContinentFound){
                currentContinent.assignNewDriftDirection(driftVelocity, world);
                currentDriftChanges++;
            }
        }
    }
}
