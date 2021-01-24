package com.watchers.model.actors.animals;

import com.watchers.model.actors.Actor;
import com.watchers.model.actors.Animal;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.AnimalType;
import com.watchers.model.enums.NaturalHabitat;
import com.watchers.model.enums.StateType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.List;

@Entity
@DiscriminatorValue(value = "WHALE")
public class Whale extends Animal {

    @Override
    public void generateOffspring(Coordinate coordinate, float foodPassed) {
        coordinate.getActors().add(new Whale(coordinate, foodPassed));
    }

    private Whale(){}

    public Whale(Coordinate coordinate, float StartingFood){
        super.setFoodReserve(StartingFood);
        super.setMaxFoodReserve(20f);
        super.setForaging(2f);
        super.setMetabolisme(1f);
        super.setReproductionRate(0.9f);
        super.setMovement(1);

        super.setAnimalType(AnimalType.WHALE);
        super.setNaturalHabitat(NaturalHabitat.AQUATIC);
        super.setStateType(StateType.ALIVE);
        super.setCoordinate(coordinate);

        List<Actor> newActors = getCoordinate().getWorld().getNewActors();
        if(newActors != null){
            newActors.add(this);
        }
    }

    @Override
    public Actor createClone(Coordinate newCoordinate) {
        return cloneBasis(new Whale(), newCoordinate);
    }
}
