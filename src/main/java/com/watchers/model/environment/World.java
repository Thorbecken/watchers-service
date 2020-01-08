package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Entity
@JsonSerialize
@Table(name = "world")
@EqualsAndHashCode(exclude= {"tiles", "continents"})
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

    @JsonProperty("continents")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "world", cascade=CascadeType.ALL)
    private Set<Continent> continents;

    public World(long xSize, long ySize){
        this.xSize = xSize;
        this.ySize = ySize;
        this.tiles = new HashSet<>();
        this.continents = new HashSet<>();
    }

    private World(){}

    @JsonIgnore
    public Tile getTile(Long x, Long y){
        if(tileMap == null || tileMap.isEmpty()){
            setTiles();
        }

        return tileMap.get(x).get(y);
    }

    private void setTiles(){
        tileMap = new HashMap<>();

        for (int i = 1; i <= xSize; i++) {
            final long xCoord = i;
            Map<Long, Tile> hashMap = new HashMap<>();
            tiles.stream()
                    .filter(tile -> tile.getXCoord() == xCoord)
                    .forEach(
                    tile -> hashMap.put(tile.getYCoord(), tile)
            );

            tileMap.put(xCoord,hashMap);
        }
    }

    @JsonIgnore
    public Set<Tile> getConcurrentTiles() {
        return new HashSet<>(tiles);
    }
}
