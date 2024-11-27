package com.watchers.model.dto;

import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.Data;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class MockContinent {
    private List<Coordinate> coordinates = new ArrayList<>();
    private Set<Coordinate> possibleCoordinates = new HashSet<>();
    private Continent continent;

    public MockContinent(Continent continent) {
        this.continent = continent;

        this.coordinates.addAll(continent.getCoordinates());
        this.coordinates.forEach(coordinate -> this.possibleCoordinates.addAll(coordinate.getNeighbours())
        );
    }

    public void generateContinent(World world) {
        Assert.isTrue(this.continent != null, "continent was null");
        continent.getCoordinates().addAll(coordinates);
        world.getCoordinates().addAll(coordinates);
        Set<Coordinate> coordinates = new HashSet<>(continent.getCoordinates());
        coordinates.forEach(
                coordinate -> {
                    coordinate.changeContinent(continent);
                    coordinate.getTile().setSurfaceType(continent.getType());
                }
        );

    }
}