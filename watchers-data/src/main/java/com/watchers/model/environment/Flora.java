package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Views;
import com.watchers.model.enums.ClimateZoneEnum;
import com.watchers.model.enums.FloraTypeEnum;
import com.watchers.model.enums.FloralImageEnum;
import com.watchers.model.enums.NaturalHabitat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
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

    @JsonView(Views.Internal.class)
    @JsonProperty("id")
    private final Long id;

    @JsonView(Views.Public.class)
    @JsonProperty("name")
    private final String name;

    @JsonView(Views.Public.class)
    @JsonProperty("natural_habitat")
    private final NaturalHabitat naturalHabitat;

    @JsonView(Views.Public.class)
    @JsonProperty("type")
    private final FloraTypeEnum type;

    @JsonView(Views.Public.class)
    @JsonProperty("image")
    private final FloralImageEnum image;

    @JsonView(Views.Public.class)
    @JsonProperty("min_temperature")
    private final double minTemperature;

    @JsonView(Views.Public.class)
    @JsonProperty("max_temperature")
    private final double maxTemperature;

    @JsonView(Views.Public.class)
    @JsonProperty("water_intake")
    private final double waterIntake;

    @JsonView(Views.Public.class)
    @JsonProperty("growth_rate")
    private final double growthRate;

    @JsonView(Views.Public.class)
    @JsonProperty("maxB_biomass")

    private final double maxBiomass;    @JsonIgnore
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


    @JsonIgnore
    static public Flora getSeawaterFlora(double maxTemperature) {
        if (maxTemperature > KELP.getMaxTemperature()) {
            return CORAL;
        } else {
            return KELP;
        }
    }

    // Inspired by AI
    public static Flora fromName(String name) {
        for (Flora flora : Flora.values()) {
            if (name.equalsIgnoreCase(flora.getName())) {
                return flora;
            }
        }
        throw new IllegalArgumentException("No Flora found with name: " + name);
    }

}
