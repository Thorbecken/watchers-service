package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Lake {

    @JsonIgnore
    private Set<Tile> lakeTiles = new HashSet<>();
    private double meanLakeHeight;
}
