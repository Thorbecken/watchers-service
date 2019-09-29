package com.watchers.model;

import javax.persistence.*;
import java.awt.*;

@Entity
@Table(name = "tile")
public class Tile {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "coordinate_id", referencedColumnName = "id")
    private Coordinate coordinate;

    @Column(name = "color")
    private Color color;

    public Tile(long xCoord, long yCoord, Color color, World world){
        this.coordinate = new Coordinate(xCoord, yCoord);
        this.world = world;
        this.color = color;
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public World getWorldId() {
        return world;
    }
}
