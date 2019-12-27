package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
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
    @OneToMany(mappedBy = "world", cascade=CascadeType.ALL)
    private Set<Tile> tiles;

    @JsonProperty("continents")
    @OneToMany(mappedBy = "world", cascade=CascadeType.ALL)
    private Set<Continent> continents;

    public World(long xSize, long ySize){
        this.xSize = xSize;
        this.ySize = ySize;
        this.tiles = new HashSet<>();
        this.continents = new HashSet<>();
    }

    private World(){

    }
}
