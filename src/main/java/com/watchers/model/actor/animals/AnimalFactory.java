package com.watchers.model.actor.animals;

import com.watchers.model.actor.Animal;
import com.watchers.model.actor.AnimalType;
import com.watchers.model.environment.Tile;

public class AnimalFactory {

    public static Animal generateNewAnimal(AnimalType animalType, Tile tile){
        switch (animalType){
            case RABBIT: return new Rabbit(tile, 1f);
            case WHALE: return new Whale(tile, 2f);
            default: return null;
        }
    }
}
