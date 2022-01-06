package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.*;
import com.watchers.model.common.Views;
import com.watchers.model.dto.MockTile;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "tile")
@SequenceGenerator(name="Tile_Gen", sequenceName="Tile_Seq", allocationSize = 1)
public class Tile {

    @Id
    @JsonProperty("tileId")
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator="Tile_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "tile_id", nullable = false)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinate_id", nullable = false)
    private Coordinate coordinate;

    @JsonProperty("height")
    @Column(name = "height")
    @JsonView(Views.Public.class)
    private long height;

    @JsonProperty("biome")
    @JsonView(Views.Public.class)
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "tile", cascade=CascadeType.ALL, orphanRemoval = true)
    private Biome biome;


    @JsonProperty("surfaceType")
    @Column(name = "surfaceType")
    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
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
                coordinate -> returnTiles.add(coordinate.getWorld().getCoordinate(coordinate.getXCoord(), coordinate.getYCoord()).getTile())
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
        this.coordinate.changeContinent(mockTile.getContinent());
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

    public Tile createClone(Coordinate newCoordinate) {
        Tile clone = new Tile();
        clone.setSurfaceType(this.surfaceType);
        clone.setCoordinate(newCoordinate);
        clone.setHeight(this.height);
        //clone.setId(this.id);
        clone.setId(newCoordinate.getId());
        clone.setBiome(this.biome.createClone(clone));
        return clone;
    }

    public Tile createBasicClone(Coordinate newCoordinate) {
        Tile clone = new Tile();
        clone.setSurfaceType(this.surfaceType);
        clone.setCoordinate(newCoordinate);
        clone.setHeight(this.height);
        clone.setId(this.id);
        clone.setBiome(this.biome.createClone(clone));
        return clone;
    }
}
