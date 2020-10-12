package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.actor.Actor;
import com.watchers.model.common.Coordinate;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Entity
@JsonSerialize
@Table(name = "world")
public class World {

    @Id
    @JsonIgnore
    @SequenceGenerator(name="World_Gen", sequenceName="World_Seq", allocationSize = 1)
    @GeneratedValue(generator="World_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "world_id")
    private Long id;

    private Long xSize;

    private Long ySize;

    @JsonProperty("coordinates")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "world", cascade=CascadeType.ALL, orphanRemoval = true)
    private Set<Coordinate> coordinates = new HashSet<>();

    @JsonProperty("continents")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "world", cascade=CascadeType.ALL, orphanRemoval = true)
    private Set<Continent> continents = new HashSet<>();

    @JsonIgnore
    private long lastContinentInFlux;

    @JsonIgnore
    private long heightDeficit;

    @Transient
    @JsonIgnore
    private Map<Long, Map<Long, Coordinate>> coordinateMap = new HashMap<>();

    @Transient
    @JsonIgnore
    private Map<Long, Map<Long, Tile>> tileMap = new HashMap<>();

    @Transient
    @JsonIgnore
    // Needs to be uninitiated for functionality, this way there can be lazy loading.
    private List<Actor> actorList;

    @Transient
    @JsonIgnore
    private List<Actor> newActors = new ArrayList<>();

    public World(long xSize, long ySize){
        this.xSize = xSize;
        this.ySize = ySize;
    }

    @SuppressWarnings("unused")
    public World(){}

public Coordinate getCoordinate(long xCoordinate, long yCoordinate) {
        if(coordinateMap == null || coordinateMap.isEmpty()){
                setCoordinateMap();
        }
        return coordinateMap.get(xCoordinate).get(yCoordinate);
    }

    @JsonIgnore
    public Tile getTile(Long x, Long y){
        if(tileMap == null || tileMap.isEmpty()){
            setTileMap();
        }

        return tileMap.get(x).get(y);
    }

    @JsonIgnore
    public Tile getTile(Coordinate coordinate){
        return getTile(coordinate.getXCoord(), coordinate.getYCoord());
    }

    @JsonIgnore
    public List<Actor> getActorList(){
        if(actorList == null){
            setActorList();
        }

        return actorList;
    }

    private void setTileMap(){
        tileMap = new HashMap<>();

        for (int i = 1; i <= xSize; i++) {
            final long xCoord = i;
            Map<Long, Tile> xTileHashMap = new HashMap<>();
            coordinates.stream()
                    .map(Coordinate::getTile)
                    .filter(tile -> tile.getCoordinate().getXCoord() == xCoord)
                    .forEach(tile -> xTileHashMap.put(tile.getCoordinate().getYCoord(), tile)
                    );

            tileMap.put(xCoord,xTileHashMap);
        }
    }

    private void setCoordinateMap(){
        coordinateMap = new HashMap<>();

        for (int i = 1; i <= xSize; i++) {
            final long xCoord = i;
            Map<Long, Coordinate> xCoordinateHashMap = new HashMap<>();
            coordinates.stream()
                    .filter(coordinate -> coordinate.getXCoord() == xCoord)
                    .forEach(coordinate -> xCoordinateHashMap.put(coordinate.getYCoord(), coordinate)
                    );

            coordinateMap.put(xCoord,xCoordinateHashMap);
        }
    }

    public void fillTransactionals() {
        newActors = new ArrayList<>();
        actorList = new ArrayList<>();
        tileMap = new HashMap<>();

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

            tileMap.put(xCoord, hashMap);
        }
    }

    private void setActorList(){
        if(tileMap == null || tileMap.isEmpty()){
            setTileMap();
        }
        actorList = new ArrayList<>();

        coordinates.forEach(
            coordinate -> actorList.addAll(coordinate.getActors())
        );
    }

    public void createBasicClone(World template){
        this.id = template.getId();
        this.xSize = template.getXSize();
        this.ySize = template.getYSize();
        this.heightDeficit = template.getHeightDeficit();
        this.lastContinentInFlux = template.getLastContinentInFlux();
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
        if (!(o instanceof World)) return false;

        World world = (World) o;

        return id != null && world.getId() != null?
                (id == null || world.getId() == null || id.equals(world.getId())):
                ySize.equals(world.ySize) && xSize.equals(world.xSize);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + xSize.hashCode();
        result = 31 * result + ySize.hashCode();
        return result;
    }
}
