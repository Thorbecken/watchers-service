package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.*;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.MockTile;
import com.watchers.model.enums.RockType;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "tile")
@SequenceGenerator(name = "Tile_Gen", sequenceName = "Tile_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Tile {

    @Id
    @JsonProperty("tileId")
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator = "Tile_Gen", strategy = GenerationType.SEQUENCE)
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

    @JsonProperty("landMoisture")
    @Column(name = "land_moisture")
    @JsonView(Views.Public.class)
    private double landMoisture;

    @JsonView(Views.Public.class)
    @JsonProperty("river")
    @OneToOne(mappedBy = "tile", cascade = CascadeType.ALL)
    private River river;

    @JsonView(Views.Public.class)
    @JsonIgnoreProperties({"world", "watershedTiles", "riverFlow"})
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Watershed watershed;

    @JsonProperty("biome")
    @JsonView(Views.Public.class)
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "tile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Biome biome;


    @JsonProperty("surfaceType")
    @Column(name = "surfaceType")
    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private SurfaceType surfaceType;

    @JsonProperty("rockType")
    @Column(name = "rock_type")
    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private RockType rockType;

    public Tile(Coordinate coordinate, Continent continent) {
        this.coordinate = coordinate;
        this.surfaceType = continent.getType();
        this.rockType = continent.getBasicRockType();
        this.biome = new Biome(this);
    }

    @JsonCreator
    @SuppressWarnings("unused")
    private Tile() {
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public List<Tile> getNeighboursWithinRange(List<Tile> tiles, int range) {
        if (range >= 1) {
            List<Tile> returnList = new ArrayList<>();
            tiles.forEach(
                    tile -> returnList.addAll(tile.getNeighbours())
            );

            return getNeighboursWithinRange(returnList, tiles, range - 1);
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
        if (range >= 1) {
            List<Tile> returnList = new ArrayList<>();
            tiles.forEach(
                    tile -> {
                        if (!oldTiles.contains(tile)) {
                            returnList.addAll(tile.getNeighbours());
                        }
                    }
            );

            return getNeighboursWithinRange(returnList, tiles, range - 1);
        } else {
            return tiles;
        }
    }

    @JsonIgnore
    public boolean isWater() {
        return surfaceType.equals(SurfaceType.OCEAN)
                || surfaceType.equals(SurfaceType.SEA)
                || surfaceType.equals(SurfaceType.COASTAL)
                || surfaceType.equals(SurfaceType.LAKE);
    }

    @JsonIgnore
    public boolean isSea() {
        return surfaceType.equals(SurfaceType.OCEAN)
                || surfaceType.equals(SurfaceType.SEA)
                || surfaceType.equals(SurfaceType.COASTAL);
    }

    @JsonIgnore
    public boolean isLand() {
        return !isWater();
    }

    public void setRiver(River river) {
        this.river = river;
        if (river != null) {
            river.setTile(this);
        }
    }

    @JsonIgnore
    public boolean riverIsConnected() {
        if (this.river == null) {
            return false;
        } else {
            return this.getNeighbours()
                    .stream()
                    .anyMatch(tile -> tile.getRiver() != null
                            || tile.isWater());
        }
    }

    public boolean coordinateEquals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;
        return coordinate.equals(((Tile) o).getCoordinate());
    }

    public void clear() {
        this.coordinate.getContinent().getCoordinates().remove(this.coordinate);
        this.height = 0;
        this.biome.clear();
    }

    public void transferData(MockTile mockTile, Coordinate survivingCoordinate) {
        this.biome.transferData(mockTile);
        if (this.river != null && survivingCoordinate.getTile().getRiver() != null) {
            this.river.mergeRivers(this.river, survivingCoordinate.getTile().getRiver());
        }
        survivingCoordinate.getActors().addAll(this.coordinate.getActors());
    }

    public void setData(MockTile mockTile, Coordinate deletedTileCoordinate) {
        Continent continent;
        if (this.getCoordinate().getContinent().getId().equals(mockTile.getMockContinentObject().getId())) {
            continent = this.coordinate.getContinent();
        } else {
            continent = this.getCoordinate().getWorld().getContinents().stream()
                    .filter(continent1 -> continent1.getId().equals(mockTile.getMockContinentObject().getId()))
                    .findFirst().orElseThrow();
        }
        this.coordinate.changeContinent(continent);
        this.coordinate.getContinent().getCoordinates().add(this.coordinate);

        this.biome.addGrassBiomass(mockTile.getGrassBiomass());
        this.biome.setGrassFlora(mockTile.getGrassFlora());
        this.biome.addTreeBiomass(mockTile.getTreeBiomass());
        this.biome.setTreeFlora(mockTile.getTreeFlora());
        this.height = mockTile.getHeight();
        this.surfaceType = mockTile.getSurfaceType();

        this.coordinate.getActors().addAll(deletedTileCoordinate.getActors());
        this.coordinate.getActors().forEach(actor -> actor.setCoordinate(coordinate));
    }

    public void changeWatershed(Watershed newWatershed) {
        this.watershed = newWatershed;
        if (this.river != null) {
            this.river.setWatershed(newWatershed);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;
        Tile tile = (Tile) o;
        return Objects.equals(id, tile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
        clone.setRockType(rockType);
        clone.setCoordinate(newCoordinate);
        clone.setHeight(this.height);
        clone.setId(newCoordinate.getId());
        clone.setBiome(this.biome.createClone(clone));
        if (watershed != null) {
            Watershed newWatershed = newCoordinate.getWorld().getWatersheds().stream()
                    .filter(oldWatershed -> oldWatershed.getId().equals(watershed.getId()))
                    .findFirst()
                    .orElseThrow();
            newWatershed.addTile(clone);
            if (this.river != null) {
                clone.setRiver(this.river.createClone(clone));
                clone.getRiver().setWatershed(newWatershed);
                newWatershed.addRiver(clone.getRiver());
            }
        }
        return clone;
    }

    public void checkIntegrity() {
        if (this.river != null) {
            this.river.checkIntegrity();
        }
    }

    public void reduceLandMoisture(double moistureReduction) {
        this.landMoisture = this.landMoisture - moistureReduction;
    }
}
