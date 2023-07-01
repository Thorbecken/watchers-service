package com.watchers.model.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.actors.Actor;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.MockCoordinate;
import com.watchers.model.environment.Tile;
import jakarta.persistence.*;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Entity
@JsonSerialize
@Table(name = "world")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class World {

    @Id
    @JsonProperty("worldId")
    @Column(name = "world_id")
    @JsonView(Views.Internal.class)
    @SequenceGenerator(name = "World_Gen", sequenceName = "World_Seq", allocationSize = 1)
    @GeneratedValue(generator = "World_Gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonProperty("xSize")
    @Column(name = "x_size")
    @JsonView(Views.Public.class)
    private Long xSize;

    @JsonProperty("ySize")
    @Column(name = "y_size")
    @JsonView(Views.Public.class)
    private Long ySize;

    @JsonProperty("worldMetaData")
    @JsonView(Views.Public.class)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorldMetaData worldMetaData;

    @JsonProperty("worldSettings")
    @JsonView(Views.Public.class)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorldSettings worldSettings;

    @JsonProperty("coordinates")
    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "world", cascade = CascadeType.ALL)
    private Set<Coordinate> coordinates = new HashSet<>();

    @JsonProperty("continents")
    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Continent> continents = new HashSet<>();

    @JsonProperty("lastContinentInFlux")
    @JsonView(Views.Internal.class)
    @Column(name = "last_continent_in_flux")
    private long lastContinentInFlux;

    @JsonProperty("heightDeficit")
    @JsonView(Views.Internal.class)
    @Column(name = "current_height_deficit")
    private long heightDeficit;

    @Transient
    @JsonIgnore
    private Map<Long, Map<Long, Coordinate>> coordinateMap = new HashMap<>();

    @Transient
    @JsonIgnore
    // Needs to be uninitiated for functionality, this way there can be lazy loading.
    private List<Actor> actorList;

    @Transient
    @JsonIgnore
    private List<Actor> newActors = new ArrayList<>();

    public World(long xSize, long ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
    }

    @SuppressWarnings("unused")
    public World() {
    }

    @JsonIgnore
    public Coordinate getCoordinate(long xCoordinate, long yCoordinate) {
        if (coordinateMap == null || coordinateMap.isEmpty()) {
            setCoordinateMap();
        }
        return coordinateMap.get(xCoordinate).get(yCoordinate);
    }


    @JsonIgnore
    public Coordinate getCoordinate(MockCoordinate mockCoordinate) {
        if (coordinateMap == null || coordinateMap.isEmpty()) {
            setCoordinateMap();
        }
        return coordinateMap.get(mockCoordinate.getXCoord())
                .get(mockCoordinate.getYCoord());
    }

    public Continent getContinentFromId(Long id) {
        return continents.stream()
                .filter(continent -> id.equals(continent.getId()))
                .findFirst()
                .orElseThrow();
    }

    @JsonIgnore
    public List<Actor> getActorList() {
        if (actorList == null || actorList.size() == 0) {
            setActorList();
        }

        return actorList;
    }

    private void setCoordinateMap() {
        coordinateMap = new HashMap<>();

        for (int i = 1; i <= xSize; i++) {
            final long xCoord = i;
            Map<Long, Coordinate> xCoordinateHashMap = new HashMap<>();
            coordinates.stream()
                    .filter(coordinate -> coordinate.getXCoord() == xCoord)
                    .forEach(coordinate -> xCoordinateHashMap.put(coordinate.getYCoord(), coordinate)
                    );

            coordinateMap.put(xCoord, xCoordinateHashMap);
        }
    }

    public void fillTransactionals() {
        newActors = new ArrayList<>();
        actorList = new ArrayList<>();

        coordinates.forEach(
                coordinate -> actorList.addAll(coordinate.getActors())
        );

        for (int i = 1; i <= xSize; i++) {
            final long xCoord = i;
            Map<Long, Tile> hashMap = new HashMap<>();
            coordinates.stream()
                    .filter(coordinate -> coordinate.getXCoord() == xCoord)
                    .forEach(
                            coordinate -> hashMap.put(coordinate.getYCoord(), coordinate.getTile())
                    );
        }
    }

    private void setActorList() {
        actorList = coordinates.stream()
                .flatMap(coordinate -> coordinate.getActors().stream())
                .collect(Collectors.toList());
    }


    @JsonIgnore
    public Long getWorldHeight(){
        return coordinates.stream()
                .map(Coordinate::getTile)
                .mapToLong(Tile::getHeight)
                .sum()
                + heightDeficit;
    }

    public World createBasicClone() {
        World newWorld = new World(this.xSize, this.ySize);
        newWorld.setId(this.id);
        newWorld.setHeightDeficit(this.heightDeficit);
        newWorld.setLastContinentInFlux(this.lastContinentInFlux);

        newWorld.setWorldSettings(worldSettings.createClone(newWorld));
        newWorld.setWorldMetaData(worldMetaData.createClone(newWorld));

        return newWorld;
    }

    @Override
    public String toString() {
        return "World{" +
                "id=" + id +
                ", xSize=" + xSize +
                ", ySize=" + ySize +
                ", coordinates=" + coordinates.size() +
                ", actorList=" + actorList.size() +
                ", continents=" + continents.size() +
                ", lastContinentInFlux=" + lastContinentInFlux +
                ", heightDeficit=" + heightDeficit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        World world = (World) o;
        return Objects.equals(id, world.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
