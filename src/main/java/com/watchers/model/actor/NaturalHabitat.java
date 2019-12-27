package com.watchers.model.actor;

import com.watchers.model.environment.SurfaceType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.watchers.model.environment.SurfaceType.CONTINENTAL;
import static com.watchers.model.environment.SurfaceType.OCEANIC;


public enum NaturalHabitat {
    AQUATIC(Collections.singletonList(OCEANIC)),
    TERRESTRIAL(Collections.singletonList(CONTINENTAL)),
    ALL(Arrays.asList(OCEANIC,CONTINENTAL));

    public final List<SurfaceType> movavableSurfaces;

    NaturalHabitat(List<SurfaceType> surfaceTypes){
        this.movavableSurfaces = surfaceTypes;
    }

}
