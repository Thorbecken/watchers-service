package com.watchers.model.environment;

import com.watchers.model.enums.ClimateZoneEnum;
import com.watchers.model.enums.FloraTypeEnum;
import com.watchers.model.enums.FloralImageEnum;
import com.watchers.model.enums.NaturalHabitat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Flora {

    GRASS(1L
            , "Grass"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.GRASS
            , FloralImageEnum.GRASS
            , ClimateZoneEnum.ARCTIC.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 0.5
            , 3
            , 10),
    PINE_TREE(2L
            , "Pine"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.PINE
            , ClimateZoneEnum.ARCTIC.getMinTemperature()
            , ClimateZoneEnum.ARCTIC.getMaxTemperature()
            , 1
            , 0.5
            , 30),
    LEAF_TREE(3L
            , "Leaf"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.LEAF
            , ClimateZoneEnum.TEMPERATE.getMinTemperature()
            , ClimateZoneEnum.TEMPERATE.getMaxTemperature()
            , 1
            , 1.5
            , 30),
    PALM_TREE(4L
            , "Palm"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.PALM
            , ClimateZoneEnum.TROPICAL.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 1
            , 1.5
            , 30),
    SAKURA_TREE(5L
            , "Sakura"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.SAKURA
            , ClimateZoneEnum.TEMPERATE.getMinTemperature()
            , ClimateZoneEnum.TEMPERATE.getMaxTemperature()
            , 1
            , 1.5
            , 30),
    WISTERIA_TREE(6L
            , "Wisteria"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.WISTERIA
            , ClimateZoneEnum.TROPICAL.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 1
            , 1.5
            , 30),
    JACARANDA_TREE(7L
            , "Jacaranda"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.JACARANDA
            , ClimateZoneEnum.TROPICAL.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 1
            , 1.5
            , 30);

    private Long id;
    private String name;
    private NaturalHabitat naturalHabitat;
    private FloraTypeEnum type;
    private FloralImageEnum image;
    private double minTemperature;
    private double maxTemperature;
    private double waterIntake;
    private double growthRate;
    private double maxBiomass;

    }
