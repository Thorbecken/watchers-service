package com.watchers.model.actor.animals;

import com.watchers.model.actor.Animal;
import com.watchers.model.actor.AnimalType;
import com.watchers.model.common.Coordinate;

public class AnimalFactory {

    public static Animal generateNewAnimal(AnimalType animalType, Coordinate coordinate){
        switch (animalType){
            case RABBIT: return new Rabbit(coordinate, 1f);
            case WHALE: return new Whale(coordinate, 2f);
            default: return null;
        }
    }
}
