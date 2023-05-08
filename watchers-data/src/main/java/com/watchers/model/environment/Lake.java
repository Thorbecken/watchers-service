package com.watchers.model.environment;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Lake {

    private Set<Tile> lakeTiles = new HashSet<>();
    private double meanLakeHeight;
}
