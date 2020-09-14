package com.watchers.model.actor.animals;

import com.watchers.model.actor.*;
import com.watchers.model.common.Coordinate;
import com.watchers.model.environment.World;

import javax.persistence.Entity;
import java.util.List;

@Entity
public class Rabbit extends Animal {
    @Override
    public void generateOffspring(Coordinate coordinate, float foodPassed) {
        coordinate.getActors().add(new Rabbit(coordinate, foodPassed));
    }

    private Rabbit(){}

    public Rabbit(Coordinate coordinate, float StartingFood){
        super.setFoodReserve(StartingFood);
        super.setMaxFoodReserve(5f);
        super.setForaging(1f);
        super.setMetabolisme(0.5f);
        super.setReproductionRate(0.8f);
        super.setMovement(1);

        super.setAnimalType(AnimalType.RABBIT);
        super.setNaturalHabitat(NaturalHabitat.TERRESTRIAL);
        super.setStateType(StateType.ALIVE);
        super.setCoordinate(coordinate);

        List<Actor> newActors = getCoordinate().getWorld().getNewActors();
        if(newActors != null){
            newActors.add(this);
        }
    }

    @Override
    public Actor createClone(Coordinate newCoordinate) {
        return cloneBasis(new Rabbit(), newCoordinate);
    }
}
