package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.actor.Actor;
import com.watchers.model.common.Coordinate;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private Set<Tile> tiles = new HashSet<>();

    @Transient
    @JsonIgnore
    private Map<Long, Map<Long, Tile>> tileMap = new HashMap<>();

    @Transient
    @JsonIgnore
    private List<Actor> actorList = new ArrayList<>();

    @Transient
    @JsonIgnore
    private List<Actor> newActors = new ArrayList<>();

    @JsonProperty("continents")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "world", cascade=CascadeType.ALL)
    private Set<Continent> continents = new HashSet<>();

    @JsonIgnore
    private long lastContinentInFlux;

    @JsonIgnore
    private long heightDeficit;

    public World(long xSize, long ySize){
        this.xSize = xSize;
        this.ySize = ySize;
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
