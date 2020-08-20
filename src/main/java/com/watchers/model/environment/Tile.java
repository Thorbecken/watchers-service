package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private Coordinate coordinate;

    @JsonProperty("height")
    @Column(name = "height")
    private long height;

    @JsonProperty("biome")
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "tile", cascade=CascadeType.ALL, orphanRemoval = true)
    private Biome biome;


    @JsonProperty("surfaceType")
    @Column(name = "surfaceType")
    private SurfaceType surfaceType;

    public Tile(Coordinate coordinate, Continent continent){
        this.coordinate = coordinate;
        this.surfaceType = continent.getType();
        this.biome = new Biome(2, 10, 0.25f, this);
    }

    @JsonCreator
    @SuppressWarnings("unused")
    private Tile(){}

    @JsonIgnore
    @SuppressWarnings("unused")
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
                coordinate -> returnTiles.add(coordinate.getWorld().getTile(coordinate.getXCoord(), coordinate.getYCoord()))
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
        this.coordinate.getContinent().getCoordinates().remove(this.coordinate);
        this.height = 0;
        this.biome.clear();
    }

    public void transferData(MockTile mockTile){
        this.biome.transferData(mockTile);
        mockTile.getActorSet().addAll(this.coordinate.getActors());
    }

    public void setData(MockTile mockTile) {
        this.coordinate.setContinent(mockTile.getContinent());
        this.coordinate.getContinent().getCoordinates().add(this.coordinate);

        this.biome.addCurrentFood(mockTile.getFood());
        this.height = mockTile.getHeight();
        this.surfaceType = mockTile.getSurfaceType();

        this.coordinate.getActors().addAll(mockTile.getActorSet());
        this.coordinate.getActors().forEach(actor -> actor.setCoordinate(coordinate));
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
                ", surfaceType=" + surfaceType +
                '}';
    }
}
