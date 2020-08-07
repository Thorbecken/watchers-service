package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.actor.Actor;
import com.watchers.model.common.Coordinate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@JsonSerialize
@Table(name = "world")
@EqualsAndHashCode(exclude= {"tiles", "continents", "tileMap", "actorList", "newActors"})
public class World {

    @Id
    @JsonIgnore
    @SequenceGenerator(name="World_Gen", sequenceName="World_Seq", allocationSize = 1)
    @GeneratedValue(generator="World_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "world_id")
    private Long id;

    private Long xSize;

    private Long ySize;

    @JsonProperty("tiles")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "world", cascade=CascadeType.ALL)
    private Set<Tile> tiles;

    @Transient
    @JsonIgnore
    private Map<Long, Map<Long, Tile>> tileMap;

    @Transient
    @JsonIgnore
    private List<Actor> actorList;

    @Transient
    @JsonIgnore
    private List<Actor> newActors;

    @JsonProperty("continents")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "world", cascade=CascadeType.ALL)
    private Set<Continent> continents;

    @JsonIgnore
    private long lastContinentInFlux;

    @JsonIgnore
    private long heightDeficit;

    public World(long xSize, long ySize){
        this.xSize = xSize;
        this.ySize = ySize;
        this.tiles = new HashSet<>();
        this.continents = new HashSet<>();

        this.newActors = new ArrayList<>();
        this.actorList = new ArrayList<>();
    }

    private World(){}

    @JsonIgnore
    public Tile getTile(Long x, Long y){
        if(tileMap == null || tileMap.isEmpty()){
            setTiles();
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

    private void setTiles(){
        tileMap = new HashMap<>();

        for (int i = 1; i <= xSize; i++) {
            final long xCoord = i;
            Map<Long, Tile> hashMap = new HashMap<>();
            tiles.stream()
                    .filter(tile -> tile.getCoordinate().getXCoord() == xCoord)
                    .forEach(tile -> hashMap.put(tile.getCoordinate().getYCoord(), tile)
            );

            tileMap.put(xCoord,hashMap);
        }
    }

    public void fillTransactionals() {
        newActors = new ArrayList<>();
        actorList = new ArrayList<>();
        tileMap = new HashMap<>();

        tiles.forEach(
                tile -> actorList.addAll(tile.getActors())
        );

        for (int i = 1; i <= xSize; i++) {
            final long xCoord = i;
            Map<Long, Tile> hashMap = new HashMap<>();
            tiles.stream()
                    .filter(tile -> tile.getCoordinate().getXCoord() == xCoord)
                    .forEach(
                            tile -> hashMap.put(tile.getCoordinate().getYCoord(), tile)
                    );

            tileMap.put(xCoord, hashMap);
        }
    }

    private void setActorList(){
        if(tileMap == null || tileMap.isEmpty()){
            setTiles();
        }
        actorList = new ArrayList<>();

        tiles.forEach(
            tile -> actorList.addAll(tile.getActors())
        );
    }

    @Override
    public String toString() {
        return "World{" +
                "id=" + id +
                ", xSize=" + xSize +
                ", ySize=" + ySize +
                ", tiles=" + tiles.size() +
                ", actorList=" + actorList.size() +
                ", continents=" + continents.size() +
                ", lastContinentInFlux=" + lastContinentInFlux +
                ", heightDeficit=" + heightDeficit +
                '}';
    }
}
