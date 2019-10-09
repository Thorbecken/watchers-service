package com.watchers.model;

import javax.persistence.*;

@Entity
@Table(name = "coordinate")
public class Coordinate {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @OneToOne(mappedBy = "coordinate")
    private Tile tile;

    @Column(name = "xCoord")
    private long xCoord;

    @Column(name = "yCoord")
    private long yCoord;

    @Column(name = "zCoord")
    private long zCoord;

    Coordinate(long xCoord, long yCoord){
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = xCoord - yCoord;
    }

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

}
