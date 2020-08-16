package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchers.model.actor.Actor;
import com.watchers.model.common.Coordinate;
import com.watchers.model.dto.MockTile;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.*;

@Data
@Entity
@Table(name = "tile")
@SequenceGenerator(name="Tile_Gen", sequenceName="Tile_Seq", allocationSize = 1)
public class Tile {

    @Id
    @JsonIgnore
    @GeneratedValue(generator="Tile_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "tile_id", nullable = false)
    private Long id;

    @JsonProperty("coordinate")
    @OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval = true)
    private Coordinate coordinate;

    @JsonProperty("height")
    @Column(name = "height")
    private long height;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonProperty("biome")
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "tile", cascade=CascadeType.ALL, orphanRemoval = true)
    private Biome biome;

    @JsonProperty("actors")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "tile", cascade=CascadeType.ALL)
    private Set<Actor> actors = new HashSet<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @JsonProperty("surfaceType")
    @Column(name = "surfaceType")
    private SurfaceType surfaceType;

    public Tile(long xCoord, long yCoord, World world, Continent continent){
        this.coordinate = new Coordinate(xCoord, yCoord, world);
        this.continent = continent;
        this.surfaceType = continent.getType();
        this.biome = new Biome(1, 10, 0.5f, this);
        this.world = world;
    }

    public Tile(Coordinate coordinate, World world, Continent continent){
        this.coordinate = new Coordinate(coordinate.getXCoord(), coordinate.getYCoord(), world);
        this.continent = continent;
        continent.getTiles().add(this);
        this.surfaceType = continent.getType();
        this.biome = new Biome(2, 10, 0.25f, this);
        this.world = world;
    }

    @JsonCreator
    private Tile(){}

    @JsonIgnore
    public Set<Actor> getConcurrentActors() {
        return new HashSet<>(actors);
    }


    @JsonIgnore
    public List<Tile> getNeighboursWithinRange(List<Tile> tiles, int range) {
        if(range>=1) {
            List<Tile> returnList = new ArrayList<>();
            tiles.forEach(
                    tile -> returnList.addAll(tile.getNeighbours())
            );

            return getNeighboursWithinRange(returnList, tiles, range-1);
        } else {
            return tiles;
        }
    }


    @JsonIgnore
    public List<Tile> getNeighbours() {
        List<Tile> returnTiles = new ArrayList<>();
        coordinate.getNeighbours().forEach(
                coordinate -> returnTiles.add(world.getTile(coordinate.getXCoord(), coordinate.getYCoord()))
        );

        return returnTiles;
    }

    @JsonIgnore
    private List<Tile> getNeighboursWithinRange(List<Tile> tiles, List<Tile> oldTiles, int range) {
        if(range>=1) {
            List<Tile> returnList = new ArrayList<>();
            tiles.forEach(
                    tile -> {
                        if(!oldTiles.contains(tile)){
                            returnList.addAll(tile.getNeighbours());
                        }
                    }
            );

            return getNeighboursWithinRange(returnList, tiles, range-1);
        } else {
            return tiles;
        }
    }

    public boolean coordinateEquals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;
        return coordinate.equals(((Tile) o).getCoordinate());
    }

    public void clear(){
        this.continent.getTiles().remove(this);
        this.continent = null;
        this.actors = new HashSet<>();
        this.height = 0;
        this.biome.clear();
    }

    public void transferData(MockTile mockTile){
        this.biome.transferData(mockTile);
        mockTile.getActorSet().addAll(this.actors);
    }

    public void setData(MockTile mockTile) {
        this.continent = mockTile.getContinent();
        mockTile.getContinent().getTiles().add(this);
        this.biome.setCurrentFood(mockTile.getFood());
        this.height = mockTile.getHeight();
        this.actors = mockTile.getActorSet();
        this.surfaceType = mockTile.getSurfaceType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;
        Tile tile = (Tile) o;
        return id != null && tile.getId() != null?
                id.equals(tile.id):
                Objects.equals(coordinate, tile.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate);
    }

    @Override
    public String toString() {
        return "Tile{" +
                "coordinate=" + coordinate.toString() +
                ", height=" + height +
                ", actors=" + actors.size() +
                ", surfaceType=" + surfaceType +
                '}';
    }
}
