package com.watchers.model.enums;

import com.watchers.model.enums.SurfaceType;

import java.util.Arrays;
import java.util.List;


public enum NaturalHabitat {
    AQUATIC(Arrays.asList(SurfaceType.OCEANIC, SurfaceType.DEEP_OCEAN, SurfaceType.COASTAL)),
    TERRESTRIAL(Arrays.asList(SurfaceType.PLAIN, SurfaceType.HILL, SurfaceType.MOUNTAIN)),
    ALL(Arrays.asList(SurfaceType.PLAIN, SurfaceType.HILL, SurfaceType.MOUNTAIN, SurfaceType.OCEANIC, SurfaceType.DEEP_OCEAN, SurfaceType.COASTAL));

    public final List<SurfaceType> movableSurfaces;

    NaturalHabitat(List<SurfaceType> surfaceTypes){
        this.movableSurfaces = surfaceTypes;
    }

}
