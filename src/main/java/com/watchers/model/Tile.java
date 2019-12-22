package com.watchers.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "tile")
@SequenceGenerator(name="Tile_Gen", sequenceName="Tile_Seq", allocationSize = 1)
@EqualsAndHashCode(exclude= {"world", "continent"})
public class Tile {

    @Id
    @JsonIgnore
    @GeneratedValue(generator="Tile_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "tile_id", nullable = false)
    private Long id;

    @JsonProperty("xCoord")
    @Column(name = "xCoord")
    private long xCoord;

    @JsonProperty("yCoord")
    @Column(name = "yCoord")
    private long yCoord;

    @JsonProperty("zCoord")
    @Column(name = "zCoord")
    private long zCoord;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @JsonProperty("landType")
    @Column(name = "landType")
    private LandType landType;

    public Tile(long xCoord, long yCoord, World world, Continent continent){
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.continent = continent;
        this.landType = continent.getType();
        this.world = world;
    }

    @JsonCreator
    private Tile(){}

    List<Tile> getNeighbours(Continent continent) {
        boolean up = yCoord > 1L;
        boolean down = yCoord < this.world.getYSize();

        List<Tile> returnTiles = new ArrayList<>();

        Tile leftTile = new Tile(getLeftCoordinate(), yCoord, this.world, continent);
        Tile rightTile =new Tile(getRightCoordinate(), yCoord, this.world, continent);

        returnTiles.add(leftTile);
        returnTiles.add(rightTile);

        if(down) {
            Tile downTile = new Tile(xCoord, yCoord - 1, this.world, continent);
            returnTiles.add(downTile);
        }
        if(up) {
            Tile upTile = new Tile(xCoord, yCoord + 1, this.world, continent);
            returnTiles.add(upTile);
        }

        return returnTiles;
    }

    private long getRightCoordinate() {
        if(xCoord == this.world.getXSize()){
            return 1;
        } else {
            return xCoord+1;
        }
    }

    private long getLeftCoordinate() {
        if(xCoord == 1){
            return this.world.getXSize();
        } else {
            return xCoord-1;
        }
    }

    public boolean coordinateEquals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;
        Tile that = (Tile) o;
        return xCoord == that.xCoord &&
                yCoord == that.yCoord;
    }

    public int coordinateHashCode() {

        return Objects.hash(xCoord, yCoord);
    }
}
