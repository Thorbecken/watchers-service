package com.watchers.model.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.helper.RandomHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RockType {
    GRANITE("GRANITE", "Grey rock"),
    Quartz("", ""),
    ORANGE_SANDSTONE("SANDSTONE", "Orange rock"),
    RED_SANDSTONE("SANDSTONE", "Red rock"),
    BROWNSTONE("BROWNSTONE", "Brown rock"),
    BASALT("BASALT", "Black rock"),
    CHALK("CHALk", "White rock");

    private String type;
    private String description;

    @JsonIgnore
    public static RockType getRandomRockType() {
        int length = RockType.values().length;
        RandomHelper.getRandom(length);
        return RockType.values()[length-1];
    }
}
