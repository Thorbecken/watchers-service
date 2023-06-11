package com.watchers.model.environment;

import com.watchers.helper.RandomHelper;
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
            , 0.25
            , 3
            , 10),
    PINE_TREE(2L
            , "Pine"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.PINE
            , ClimateZoneEnum.ARCTIC.getMinTemperature()
            , ClimateZoneEnum.ARCTIC.getMaxTemperature()
            , 0.5
            , 0.5
            , 30),
    LEAF_TREE(3L
            , "Leaf"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.LEAF
            , ClimateZoneEnum.TEMPERATE.getMinTemperature()
            , ClimateZoneEnum.TEMPERATE.getMaxTemperature()
            , 0.5
            , 1.5
            , 30),
    PALM_TREE(4L
            , "Palm"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.PALM
            , ClimateZoneEnum.TROPICAL.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 0.5
            , 1.5
            , 30),
    SAKURA_TREE(5L
            , "Sakura"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.SAKURA
            , ClimateZoneEnum.TEMPERATE.getMinTemperature()
            , ClimateZoneEnum.TEMPERATE.getMaxTemperature()
            , 0.5
            , 1.5
            , 30),
    WISTERIA_TREE(6L
            , "Wisteria"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.WISTERIA
            , ClimateZoneEnum.TROPICAL.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 0.5
            , 1.5
            , 30),
    JACARANDA_TREE(7L
            , "Jacaranda"
            , NaturalHabitat.TERRESTRIAL
            , FloraTypeEnum.TREE
            , FloralImageEnum.JACARANDA
            , ClimateZoneEnum.TROPICAL.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 0.5
            , 1.5
            , 30),

    KELP(8L
            , "Kelp"
            , NaturalHabitat.SALT_WATER
            , FloraTypeEnum.TREE
            , FloralImageEnum.KELP
            , ClimateZoneEnum.ARCTIC.getMinTemperature()
            , ClimateZoneEnum.TEMPERATE.getMaxTemperature()
            , 0
            , 3
            , 10),

    CORAL(8L
            , "Coral"
            , NaturalHabitat.SALT_WATER
            , FloraTypeEnum.TREE
            , FloralImageEnum.CORAL
            , ClimateZoneEnum.TROPICAL.getMinTemperature()
            , ClimateZoneEnum.TROPICAL.getMaxTemperature()
            , 0
            , 3
            , 10);

    private final Long id;
    private final String name;
    private final NaturalHabitat naturalHabitat;
    private final FloraTypeEnum type;
    private final FloralImageEnum image;
    private final double minTemperature;
    private final double maxTemperature;
    private final double waterIntake;
    private final double growthRate;
    private final double maxBiomass;

    static public Flora getTreeFlora(double maxTemperature) {
        if (maxTemperature < PINE_TREE.getMaxTemperature())
            return PINE_TREE;
        if (maxTemperature < LEAF_TREE.getMaxTemperature()) {
            int x = RandomHelper.getRandom(2);
            switch (x) {
                case 0:
                    return LEAF_TREE;
                case 1:
                    return SAKURA_TREE;
            }
        }
        if (maxTemperature < PALM_TREE.getMaxTemperature()) {
            int x = RandomHelper.getRandom(3);
            switch (x) {
                case 0:
                    return PALM_TREE;
                case 1:
                    return WISTERIA_TREE;
                case 2:
                    return JACARANDA_TREE;
            }
        }
        return null;
    }

    static public Flora getBasicTreeFlora(double maxTemperature) {
        if (maxTemperature < PINE_TREE.getMaxTemperature())
            return PINE_TREE;
        if (maxTemperature < LEAF_TREE.getMaxTemperature()) {
            int random = RandomHelper.getRandom(1);
            switch (random) {
                case 0:
                    return LEAF_TREE;
                case 1:
                    return SAKURA_TREE;
                default:
                    return LEAF_TREE;
            }
        }
        if (maxTemperature < PALM_TREE.getMaxTemperature()) {
            int random = RandomHelper.getRandom(2);
            switch (random) {
                case 0:
                    return PALM_TREE;
                case 1:
                    return JACARANDA_TREE;
                case 2:
                    return WISTERIA_TREE;
                default:
                    return PALM_TREE;
            }
        }
        return LEAF_TREE;
    }


    static public Flora getSeawaterFlora(double maxTemperature) {
        if (maxTemperature > KELP.getMaxTemperature()) {
            return CORAL;
        } else {
            return KELP;
        }
    }
}
