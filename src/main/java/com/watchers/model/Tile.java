package com.watchers.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.awt.*;

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

    @JsonProperty("color")
    @Column(name = "color")
    private Color color;

    public Tile(long xCoord, long yCoord, Color color, World world){
        this.coordinate = new Coordinate(xCoord, yCoord, this);
        this.world = world;
        this.color = color;
    }

    @JsonCreator
    private Tile(){}

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

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
}
