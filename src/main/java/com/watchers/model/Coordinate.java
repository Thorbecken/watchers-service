package com.watchers.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Table(name = "coordinate")
public class Coordinate {

    @Id
    @JsonIgnore
    @GeneratedValue
    @Column(name = "coordinate_id")
    private Long id;

    @JsonIgnore
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "tile_id", nullable = false)
    private Tile tile;

    @JsonProperty("xCoord")
    @Column(name = "xCoord")
    private long xCoord;

    @JsonProperty("yCoord")
    @Column(name = "yCoord")
    private long yCoord;

    @JsonProperty("zCoord")
    @Column(name = "zCoord")
    private long zCoord;

    Coordinate(long xCoord, long yCoord, Tile tile){
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = xCoord - yCoord;
        this.tile = tile;
    }

    @JsonCreator
    private Coordinate(){}

    public long getxCoord() {
        return xCoord;
    }

    public long getyCoord() {
        return yCoord;
    }

    public long getzCoord() {
        return zCoord;
    }

    @JsonIgnore
    public Tile getTile() {
        return tile;
    }
}
