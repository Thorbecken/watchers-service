package com.watchers.model.special.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointOfInterestType {
    TECTONIC_CRYSTAL ("TectonicCrystal"),
    HOT_SPOT_CRYSTAL ("HotSpotCrystal"),
    AQUIFER_CRYSTAL ("AquiferCrystal"),
    WIND_CRYSTAL ("WindCrystal"),
    GREAT_FLORA("GreatFlora");

    private final String name;
}
