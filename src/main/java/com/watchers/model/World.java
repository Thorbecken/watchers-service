package com.watchers.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.util.Set;

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

    @JsonProperty("tiles")
    @OneToMany(mappedBy = "world", cascade=CascadeType.ALL)
    private Set<Tile> tiles;

    public Set<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(Set<Tile> tiles) {
        this.tiles = tiles;
    }
}
