package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@JsonSerialize
@Table(name = "continent")
public class Continent {

    @Id
    @JsonIgnore
    @SequenceGenerator(name="Continent_Gen", sequenceName="Continent_Seq", allocationSize = 1)
    @GeneratedValue(generator="Continent_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "continent_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonProperty("tiles")
    @OneToMany(mappedBy = "continent", cascade=CascadeType.ALL)
    private Set<Tile> tiles;

    @JsonProperty("type")
    private SurfaceType type;

    public Continent(World world, SurfaceType surfaceType){
        this.tiles = new HashSet<>();
        this.world = world;
        this.type = surfaceType;
    }

    private Continent(){}
}
