package com.watchers.model.actor;

import com.watchers.model.environment.SurfaceType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.watchers.model.environment.SurfaceType.*;


public enum NaturalHabitat {
    AQUATIC(Arrays.asList(OCEANIC,DEEP_OCEAN,COASTAL)),
    TERRESTRIAL(Collections.singletonList(CONTINENTAL)),
    ALL(Arrays.asList(OCEANIC,CONTINENTAL));

    public final List<SurfaceType> movavableSurfaces;

    NaturalHabitat(List<SurfaceType> surfaceTypes){
        this.movavableSurfaces = surfaceTypes;
    }

}
