package com.watchers.model;

import javax.persistence.*;
import java.awt.*;

@Entity
public class Tile {

   /* @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @ManyToOne
    private long WorldId;

    @OneToOne
    @JoinTable
    private Coordinate coordinate;

    @Column(name = "color")
    private Color color;

    public Tile(long xCoord, long yCoord, Color color, long worldId){
        this.coordinate = new Coordinate(xCoord, yCoord);
        this.WorldId = worldId;
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

    public long getWorldId() {
        return WorldId;
    }*/
}
