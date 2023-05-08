package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.*;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.dto.MockTile;
import com.watchers.model.enums.DirectionEnum;
import com.watchers.model.enums.RockType;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.Continent;
import com.watchers.pathfinding.GraphNode;
import lombok.Data;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "tile")
@SequenceGenerator(name = "Tile_Gen", sequenceName = "Tile_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Tile implements GraphNode {

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

    @Column(name = "lake")
    @JsonView(Views.Public.class)
    private boolean isLakeTile;

    @Column(name = "river")
    @JsonView(Views.Public.class)
    private boolean isRiver;

    @Column(name = "large_river")
    @JsonView(Views.Public.class)
    private boolean isLargeRiver;

    @Column(name = "coastal_land")
    @JsonView(Views.Public.class)
    private boolean isCoastalLand;

    @Transient
    private Lake lake;

    @Transient
    @JsonIgnore
    private Tile downWardTile;

    public void setDownWardTile(Tile downWardTile) {
        this.downWardTile = downWardTile;
        downWardTile.getUpwardTiles().add(this);
        long yDifference = this.downWardTile.getCoordinate().getYCoord() - this.coordinate.getYCoord();
        long xDifference = this.downWardTile.getCoordinate().getXCoord() - this.coordinate.getXCoord();
        if (yDifference > 0L) {
            this.flowDirection = DirectionEnum.DOWN;
        } else if (yDifference < 0L) {
            this.flowDirection = DirectionEnum.UP;
        } else if (xDifference > 0L) {
            this.flowDirection = DirectionEnum.RIGHT;
        } else {
            this.flowDirection = DirectionEnum.LEFT;
        }
    }

    @Column(name = "down_flow_Amount")
    @JsonView(Views.Public.class)
    private double downFlowAmount;

    @Column(name = "flow_direction")
    @JsonView(Views.Public.class)
    private DirectionEnum flowDirection;

    @Transient
    @JsonIgnore
    private List<Tile> upwardTiles = new ArrayList<>();

    public void resetDownflowValues() {
        upwardTiles.clear();
        downWardTile = null;
        downFlowAmount = 0d;
        isRiver = false;
        isLakeTile = false;
        isLargeRiver = false;
        flowDirection = null;
    }

    @JsonIgnore
    public boolean readyToFlow() {
        return downFlowAmount == 0d
                && !upwardTiles.isEmpty()
                && upwardTiles.stream()
                .allMatch(tile -> tile.downFlowAmount != 0d);
    }

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
        this.id = coordinate.getWorld().getCoordinates().stream()
                .map(Coordinate::getTile)
                .mapToLong(Tile::getId)
                .max()
                .orElse(0); // the zero is to let the first id be one
        this.id = this.id + 1;
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
        return clone;
    }

    public void checkIntegrity() {
    }

    public void reduceLandMoisture(double moistureReduction) {
        this.landMoisture = this.landMoisture - moistureReduction;
    }

    @JsonIgnore
    public double getDistance(Tile to) {
        return this.coordinate.getDistance(to.coordinate);
    }


    @JsonIgnore
    public List<Tile> getLowerHeightTilesOrderedByHeightDescending() {
        return coordinate.getLowerHeightCoordinatesNeighbours().stream()
                .map(Coordinate::getTile)
                .filter(tile -> tile != this)
                .sorted(Comparator.comparing(Tile::getHeight).reversed())
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Set<Long> getLowerOrEqualHeightLandTileIds() {
        return coordinate.getLowerOrEqualHeightLandCoordinatesWithinRange(1).stream()
                .map(Coordinate::getTile)
                .map(Tile::getId)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Long> getNeighbourIds() {
        return this.getNeighbours().stream()
                .map(Tile::getId)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public boolean isNeighbour(Tile tile) {
        return this.coordinate.isNeigbour(tile.getCoordinate());
    }

    @JsonIgnore
    public boolean isLowestPoint() {
        return this.getNeighbours().stream()
                .noneMatch(neighbour -> neighbour.getHeight() <= this.height);
    }

    @JsonIgnore
    public boolean hasLowerHeightNeighbour() {
        return this.getNeighbours().stream()
                .anyMatch(neighbour -> neighbour.getHeight() < this.height);
    }

    @JsonIgnore
    public List<Tile> getSameHeightNeigbours() {
        return this.getNeighbours().stream()
                .filter(neighbour -> neighbour.getHeight() == this.height)
                .collect(Collectors.toList());
    }
}
