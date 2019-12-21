package com.watchers.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @JsonProperty("coordinate")
    @OneToOne(mappedBy = "tile", cascade=CascadeType.ALL)
    private Coordinate coordinate;

    @JsonProperty("landType")
    @Column(name = "landType")
    private LandType landType;

    public Tile(long xCoord, long yCoord, World world, Continent continent){
        this.coordinate = new Coordinate(xCoord, yCoord, this);
        this.continent = continent;
        this.landType = continent.getType();
        this.world = world;
    }

    @JsonCreator
    private Tile(){}

    List<Tile> getNeighbours(Continent continent) {
        long xCoordinate = this.coordinate.getXCoord();
        long yCoordinate = this.coordinate.getYCoord();

        boolean up = yCoordinate > 1L;
        boolean down = yCoordinate < this.world.getYSize();

        List<Tile> returnTiles = new ArrayList<>();

        Tile leftTile = new Tile(getLeftCoordinate(xCoordinate), yCoordinate, this.world, continent);
        Tile rightTile =new Tile(getRightCoordinate(xCoordinate), yCoordinate, this.world, continent);

        returnTiles.add(leftTile);
        returnTiles.add(rightTile);

        if(down) {
            Tile downTile = new Tile(xCoordinate, yCoordinate - 1, this.world, continent);
            returnTiles.add(downTile);
        }
        if(up) {
            Tile upTile = new Tile(xCoordinate, yCoordinate + 1, this.world, continent);
            returnTiles.add(upTile);
        }

        return returnTiles;
        /*
        List<Tile> list = Arrays.asList(
                new Tile(getLeftCoordinate(xCoordinate), yCoordinate, this.world, continent),
                new Tile(getRightCoordinate(xCoordinate), yCoordinate, this.world, continent),
                yCoordinate == 1l ? null : new Tile(xCoordinate, yCoordinate-1, this.world, continent),
                yCoordinate == this.world.getySize() ? new Tile(xCoordinate, yCoordinate+1, this.world, continent) : null
                );

        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        */
    }

    private long getRightCoordinate(long xCoordinate) {
        if(xCoordinate == this.world.getXSize()){
            return 1;
        } else {
            return xCoordinate+1;
        }
    }

    private long getLeftCoordinate(long xCoordinate) {
        if(xCoordinate == 1){
            return this.world.getXSize();
        } else {
            return xCoordinate-1;
        }
    }
}
