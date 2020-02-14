package com.watchers.model.actor.animals;

import com.watchers.model.actor.*;
import com.watchers.model.environment.Tile;

import javax.persistence.Entity;
import java.util.List;

@Entity
public class Whale extends Animal {

    @Override
    public void generateOffspring(Tile tile, float foodPassed) {
        tile.getActors().add(new Whale(tile, foodPassed));
    }

    private Whale(){}

    public Whale(Tile tile, float StartingFood){
        super.setFoodReserve(StartingFood);
        super.setMaxFoodReserve(20f);
        super.setForaging(2f);
        super.setMetabolisme(1f);
        super.setReproductionRate(0.9f);
        super.setMovement(1);

        super.setAnimalType(AnimalType.WHALE);
        super.setNaturalHabitat(NaturalHabitat.AQUATIC);
        super.setStateType(StateType.ALIVE);
        super.setTile(tile);

        List<Actor> newActors = getTile().getWorld().getNewActors();
        if(newActors != null){
            newActors.add(this);
        }
    }
}
