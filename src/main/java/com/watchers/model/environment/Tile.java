package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchers.model.actor.Actor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "tile")
@SequenceGenerator(name="Tile_Gen", sequenceName="Tile_Seq", allocationSize = 1)
@EqualsAndHashCode(exclude= {"world", "continent", "actors", "biome"})
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

    @JsonProperty("biome")
    @OneToOne(mappedBy = "tile", cascade=CascadeType.ALL)
    private Biome biome;

    @JsonProperty("actors")
    @OneToMany(mappedBy = "tile", cascade=CascadeType.ALL)
    private Set<Actor> actors;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @JsonProperty("surfaceType")
    @Column(name = "surfaceType")
    private SurfaceType surfaceType;

    public Tile(long xCoord, long yCoord, World world, Continent continent){
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.continent = continent;
        this.surfaceType = continent.getType();
        this.actors = new HashSet<>();
        this.biome = new Biome(1, 10, 1, this);
        this.world = world;
    }

    @JsonCreator
    private Tile(){}

    @JsonIgnore
    public List<Tile> getNeighbours() {
        boolean down = yCoord > 1L;
        boolean up = yCoord < this.world.getYSize();

        List<Tile> returnTiles = new ArrayList<>();
        returnTiles.add(getNeighbouringTile(getLeftCoordinate(), yCoord));
        returnTiles.add(getNeighbouringTile(getRightCoordinate(), yCoord));


        if(down) {
            Tile downTile = getNeighbouringTile(xCoord, yCoord - 1);
            returnTiles.add(downTile);
        }
        if(up) {
            Tile upTile = getNeighbouringTile(xCoord, yCoord + 1);
            returnTiles.add(upTile);
        }

        return returnTiles;
    }

/*
    public List<Tile> getNeighboursInRange(int range) {
        List<Tile> neighbours = getNeighbours();
        for (int i = 1; i < range; i++) {

        }
        return neighbours;
    }
*/

    @JsonIgnore
    private Tile getNeighbouringTile(long xCoord, long yCoord) {
        return world.getTiles().stream()
                .filter(
                        worldTile -> worldTile.getXCoord() == xCoord && worldTile.getYCoord() == yCoord
                ).findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public List<Tile> getNeighboursContinental(Continent continent) {
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

    @JsonIgnore
    private long getRightCoordinate() {
        if(xCoord == this.world.getXSize()){
            return 1;
        } else {
            return xCoord+1;
        }
    }

    @JsonIgnore
    private long getLeftCoordinate() {
        if(xCoord == 1){
            return this.world.getXSize();
        } else {
            return xCoord-1;
        }
    }

    boolean coordinateEquals(Object o) {
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