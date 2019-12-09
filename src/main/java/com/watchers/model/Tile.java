package com.watchers.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Table(name = "tile")
@SequenceGenerator(name="Tile_Gen", sequenceName="Tile_Seq", allocationSize = 1)
public class Tile {

    @Id
    @JsonIgnore
    @GeneratedValue(generator="Tile_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "tile_id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonProperty("coordinate")
    @OneToOne(mappedBy = "tile", cascade=CascadeType.ALL)
    private Coordinate coordinate;

    @JsonProperty("landType")
    @Column(name = "landType")
    private String landType;

    public Tile(long xCoord, long yCoord, World world){
        this.coordinate = new Coordinate(xCoord, yCoord, this);
        this.world = world;
    }

    @JsonCreator
    private Tile(){}

    public Long getId() {
        return id;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    @JsonIgnore
    public World getWorldId() {
        return world;
    }

    public String getLandType() {
        return landType;
    }

    public void setLandType(String landType) {
        this.landType = landType;
    }
}
