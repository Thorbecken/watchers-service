package com.watchers.model.enums;

import java.util.Arrays;
import java.util.List;


public enum NaturalHabitat {
    SALT_WATER(Arrays.asList(SurfaceType.OCEAN, SurfaceType.SEA, SurfaceType.COASTAL)),
    FRESH_WATER(Arrays.asList(SurfaceType.LAKE, SurfaceType.LARGE_RIVER)),
    AQUATIC(Arrays.asList(SurfaceType.OCEAN, SurfaceType.SEA, SurfaceType.COASTAL)),
    TERRESTRIAL(Arrays.asList(SurfaceType.PLAIN, SurfaceType.HILL, SurfaceType.MOUNTAIN)),
    ALL(Arrays.asList(SurfaceType.OCEAN, SurfaceType.SEA, SurfaceType.COASTAL,
            SurfaceType.LAKE, SurfaceType.LARGE_RIVER,
            SurfaceType.PLAIN, SurfaceType.HILL, SurfaceType.MOUNTAIN));

    public final List<SurfaceType> movableSurfaces;

    NaturalHabitat(List<SurfaceType> surfaceTypes){
        this.movableSurfaces = surfaceTypes;
    }

}
