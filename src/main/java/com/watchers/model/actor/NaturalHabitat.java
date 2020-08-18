package com.watchers.model.actor;

import com.watchers.model.environment.SurfaceType;

import java.util.Arrays;
import java.util.List;

import static com.watchers.model.environment.SurfaceType.*;


public enum NaturalHabitat {
    AQUATIC(Arrays.asList(OCEANIC,DEEP_OCEAN,COASTAL)),
    TERRESTRIAL(Arrays.asList(PLAIN, HILL, MOUNTAIN)),
    ALL(Arrays.asList(PLAIN, HILL, MOUNTAIN, OCEANIC,DEEP_OCEAN,COASTAL));

    public final List<SurfaceType> movavableSurfaces;

    NaturalHabitat(List<SurfaceType> surfaceTypes){
        this.movavableSurfaces = surfaceTypes;
    }

}
