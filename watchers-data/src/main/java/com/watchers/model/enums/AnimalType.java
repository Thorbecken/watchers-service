package com.watchers.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AnimalType {
    RABBIT(1L
            , "Rabbit"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    BIRD(2L
            , "Bird"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    BOAR(3L
            , "Boar"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    CAMEL(4L
            , "Camel"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    COW(5L
            , "Cow"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    DEER(6L
            , "Deer"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    EAGLE(7L
            , "Eagle"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    GOAT(8L
            , "Goat"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    HORSE(9L
            , "Horse"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    LION(10L
            , "Lion"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    MONKEY(11L
            , "Monkey"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    PENGUIN(12L
            , "Penguin"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    SHEEP(13L
            , "Sheep"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    TIGER(14L
            , "Tiger"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),
    WOLF(15L
            , "Wolf"
            , 5f
            , 1f
            , 0.5f
            , 0.8f
            , 1
            , NaturalHabitat.TERRESTRIAL),

    WHALE(16L
            , "Whale"
            , 20f
            , 2f
            , 1f
            , 0.9f
            , 1
            , NaturalHabitat.AQUATIC);

    @JsonProperty("id")
    @JsonView(Views.Internal.class)
    private final Long id;

    @JsonView(Views.Public.class)
    @JsonProperty("name")
    private final String name;

    @JsonView(Views.Public.class)
    @JsonProperty("max_food_reserve")
    private final float maxFoodReserve;

    @JsonView(Views.Public.class)
    @JsonProperty("foraging")
    private final float foraging;

    @JsonView(Views.Public.class)
    @JsonProperty("metabolism")
    private final float metabolism;

    @JsonView(Views.Public.class)
    @JsonProperty("reproduction_rate")
    private final float reproductionRate;

    @JsonView(Views.Public.class)
    @JsonProperty("movement")
    private final int movement;

    @JsonView(Views.Public.class)
    @JsonProperty("natural_habitat")
    private final NaturalHabitat naturalHabitat;

    // Inspired by AI
    public static AnimalType fromName(String name) {
        for (AnimalType animalType : AnimalType.values()) {
            if (name.equalsIgnoreCase(animalType.getName())) {
                return animalType;
            }
        }
        throw new IllegalArgumentException("No AnimalType found with name: " + name);
    }


}
