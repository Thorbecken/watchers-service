package com.watchers.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

public class Coordinate {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

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
