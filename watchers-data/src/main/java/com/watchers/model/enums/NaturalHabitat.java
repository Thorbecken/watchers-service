package com.watchers.model.enums;

import com.watchers.model.enums.SurfaceType;

import java.util.Arrays;
import java.util.List;


public enum NaturalHabitat {
    AQUATIC(Arrays.asList(SurfaceType.OCEAN, SurfaceType.SEA, SurfaceType.COASTAL)),
    TERRESTRIAL(Arrays.asList(SurfaceType.PLAIN, SurfaceType.HILL, SurfaceType.MOUNTAIN)),
    ALL(Arrays.asList( SurfaceType.OCEAN, SurfaceType.SEA, SurfaceType.COASTAL, SurfaceType.PLAIN, SurfaceType.HILL, SurfaceType.MOUNTAIN));

    public final List<SurfaceType> movableSurfaces;

    NaturalHabitat(List<SurfaceType> surfaceTypes){
        this.movableSurfaces = surfaceTypes;
    }

}
