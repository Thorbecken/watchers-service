package com.watchers.model.world;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.helper.RandomHelper;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.common.Direction;
import com.watchers.model.common.Views;
import com.watchers.model.enums.SurfaceType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
@Entity
@JsonSerialize
@Table(name = "continent")
public class Continent {

    @Id
    @JsonProperty("continentId")
    @Column(name = "continent_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "Continent_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Continent_Gen", sequenceName = "Continent_Seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "continent")
    private Set<Coordinate> coordinates = new HashSet<>();

    @JsonProperty("surfaceType")
    @Column(name = "surface_type")
    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private SurfaceType type;

    @JsonProperty("direction")
    @JsonView(Views.Public.class)
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    private Direction direction;

    public Continent(World world, SurfaceType surfaceType) {
        this.world = world;
        this.world.getContinents().add(this);
        this.type = surfaceType;
        this.assignNewDriftDirection(1, world);
    }

    @JsonCreator
    @SuppressWarnings("unused")
    private Continent() {
    }

    /**
     * @param driftVelocity the speed at which the new directions of the continent can be
     * @return the continent which direction has been changed on the x and y axis.
     * These can be possitive or negative (left, right, up down).
     */
    public Continent assignNewDriftDirection(int driftVelocity, World world) {
        if (this.direction == null) {
            int xVelocity = RandomHelper.getRandomWithNegativeNumbers(driftVelocity);
            int yVelocity = RandomHelper.getRandomWithNegativeNumbers(driftVelocity);

            Direction direction = new Direction(xVelocity, yVelocity);
            this.setDirection(direction);
        } else {
            this.direction.setXVelocity(RandomHelper.getRandomWithNegativeNumbers(driftVelocity));
            this.direction.setYVelocity(RandomHelper.getRandomWithNegativeNumbers(driftVelocity));
            log.warn("setting last continent in flux to " + this.getId() + " from continent");
            world.setLastContinentInFlux(this.getId());
        }

        return this;
    }

    public void addCoordinate(Coordinate coordinate) {
        coordinates.add(coordinate);
        if (!coordinate.getContinent().equals(this)) {
            coordinate.changeContinent(this);
        }
    }

    public void removeCoordinate(Coordinate coordinate) {
        boolean removed = this.getCoordinates().remove(coordinate);
        if (removed) {
            coordinate.changeContinent(null);
        }
    }

    @Override
    public String toString() {
        return "Continent{" +
                "id=" + id +
                ", world=" + world.getId() +
                ", coordinates=" + coordinates.size() +
                ", type=" + type +
                ", direction=" + direction.toString() +
                '}';
    }

    public Continent createClone(World newWorld) {
        Continent clone = new Continent();
        clone.setId(this.id);
        clone.setType(this.type);
        clone.setWorld(newWorld);
        clone.setDirection(this.direction.createClone());
        return clone;
    }

    @JsonIgnore
    public Long calculateMostConnectedNeighbouringContinent() {
        Map<Long, List<Continent>> list = coordinates.stream()
                .map(Coordinate::getNeighbours)
                .flatMap(Collection::stream)
                .map(Coordinate::getContinent)
                .filter(continent -> !this.id.equals(continent.getId()))
                .collect(Collectors.groupingBy(Continent::getId));

        Optional<Long> mostConnectedNeighbouringContinent = list.keySet().stream()
                .max(Comparator.comparingInt((Long key) -> list.get(key).size()));

        return mostConnectedNeighbouringContinent.orElseGet(this::getId);
    }
}
