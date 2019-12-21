package com.watchers.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Data
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) o;
        return xCoord == that.xCoord &&
                yCoord == that.yCoord;
    }

    @Override
    public int hashCode() {

        return Objects.hash(xCoord, yCoord);
    }
}
