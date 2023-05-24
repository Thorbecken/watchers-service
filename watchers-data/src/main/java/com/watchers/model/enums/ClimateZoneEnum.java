package com.watchers.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClimateZoneEnum {

    ARCTIC(-24d, -7d),
    TEMPERATE(-7d, 17d),
    TROPICAL(17d, 30d);

    private final double minTemperature;
    private final double maxTemperature;

}
