package com.watchers.model.enums;

import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.Column;

@Getter
@AllArgsConstructor
public enum AnimalType {
    RABBIT(1L
    ,"Rabbit"
    ,5f
    , 1f
    , 0.5f
    , 0.8f
    , 1
    , NaturalHabitat.TERRESTRIAL),

    WHALE(2L
    ,"Whale"
    , 20f
    , 2f
    , 1f
    ,0.9f
    , 1
    , NaturalHabitat.AQUATIC);

    private Long id;
    private String name;
    private float maxFoodReserve;
    private float foraging;
    private float metabolisme;
    private float reproductionRate;
    private int movement;
    private NaturalHabitat naturalHabitat;
}
