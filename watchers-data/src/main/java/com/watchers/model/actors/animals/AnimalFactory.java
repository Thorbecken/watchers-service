package com.watchers.model.actors.animals;

import com.watchers.model.actors.Animal;
import com.watchers.model.common.Coordinate;
import com.watchers.model.enums.AnimalType;

public class AnimalFactory {

    public static Animal generateNewAnimal(AnimalType animalType, Coordinate coordinate){
        switch (animalType){
            case RABBIT: return new Rabbit(coordinate, 1f);
            case WHALE: return new Whale(coordinate, 2f);
            default: return null;
        }
    }
}
