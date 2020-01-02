package com.watchers.model.actor.animals;

import com.watchers.model.actor.Animal;
import com.watchers.model.actor.AnimalType;
import com.watchers.model.actor.NaturalHabitat;
import com.watchers.model.actor.StateType;
import com.watchers.model.environment.Tile;

import javax.persistence.Entity;

@Entity
public class Rabbit extends Animal {
    @Override
    public void generateOffspring(Tile tile, float foodPassed) {
        tile.getActors().add(new Rabbit(tile, foodPassed));
    }

    private Rabbit(){}

    public Rabbit(Tile tile, float StartingFood){
        super.setFoodReserve(StartingFood);
        super.setMaxFoodReserve(5f);
        super.setForaging(1f);
        super.setMetabolisme(0.5f);
        super.setReproductionRate(0.8f);
        super.setMovement(1);

        super.setAnimalType(AnimalType.RABBIT);
        super.setNaturalHabitat(NaturalHabitat.TERRESTRIAL);
        super.setStateType(StateType.ALIVE);
        super.setTile(tile);
    }
}
