package com.watchers.model.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.helper.RandomHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RockType {
    GRANITE("GRANITE", "Grey rock", 25d),
    QUARTZ("QUARTZ", "Quartz", 25d),
    ORANGE_SANDSTONE("SANDSTONE", "Orange rock", 25d),
    RED_SANDSTONE("SANDSTONE", "Red rock", 25d),
    BROWNSTONE("BROWNSTONE", "Brown rock", 25d),
    BASALT("BASALT", "Black rock", 25d),
    CHALK("CHALk", "White rock", 25d);

    private final String type;
    private final String description;

    private final double maxWaterRetention;

    @JsonIgnore
    public static RockType getRandomRockType() {
        int length = RockType.values().length;
        int random = RandomHelper.getRandom(length);
        return RockType.values()[random];
    }
}
