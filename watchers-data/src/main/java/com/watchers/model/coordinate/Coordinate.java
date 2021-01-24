package com.watchers.model.coordinate;

import com.fasterxml.jackson.annotation.*;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.climate.Climate;
import com.watchers.model.common.Views;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
import com.watchers.model.actors.Actor;
import com.watchers.model.world.Continent;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "coordinate")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="coordinate_type", discriminatorType = DiscriminatorType.STRING)
@SequenceGenerator(name="Coordinate_Gen", sequenceName="Coordinate_Seq", allocationSize = 1)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GlobeCoordinate.class, name = "GlobeCoordinate"),
        @JsonSubTypes.Type(value = NonEuclideanCoordinate.class, name = "NonEuclideanCoordinate"),
        @JsonSubTypes.Type(value = WrapAroundCoordinate.class, name = "WrapAroundCoordinate")
})
public abstract class Coordinate {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("coordinateId")
    @GeneratedValue(generator="Coordinate_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "coordinate_id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private CoordinateType coordinateType;

    @JsonProperty("xCoord")
    @Column(name = "xCoord")
    @JsonView(Views.Public.class)
    private long xCoord;

    @JsonProperty("yCoord")
    @Column(name = "yCoord")
    @JsonView(Views.Public.class)
    private long yCoord;

    @JsonProperty("tile")
    @JsonView(Views.Public.class)
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade=CascadeType.ALL, orphanRemoval = true)
    private Tile tile;

    @JsonProperty("actors")
    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade=CascadeType.ALL, orphanRemoval = true)
    private Set<Actor> actors = new HashSet<>();

    @JsonView(Views.Public.class)
    @JsonIgnoreProperties({"world", "coordinates", "type" })
    @ManyToOne(fetch = FetchType.EAGER)
    private Continent continent;

    @JsonView(Views.Public.class)
    @JsonProperty("climate")
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade=CascadeType.ALL, orphanRemoval = true)
    private Climate climate;

    protected Coordinate(long xCoord, long yCoord, CoordinateType coordinateType, World world, Continent continent) {
        this.yCoord = yCoord;
        this.xCoord = xCoord;
        this.world = world;
        this.continent = continent;
        this.coordinateType = coordinateType;

        this.tile = new Tile(this, continent);
        this.climate = new Climate(this);
    }

    private void setContinent(Continent continent){
        this.continent = continent;
    }

    public void changeContinent(Continent newContinent){
        if(continent != null) {
            continent.removeCoordinate(this);
        }
        this.continent = newContinent;
        if(newContinent != null){
            newContinent.addCoordinate(this);
        }
    }

    @JsonIgnore
    public abstract List<Coordinate> getNeighbours();

    @JsonIgnore
    public long getRightCoordinate() {
        return getAdjustedXCoordinate(CoordinateHelper.RIGHT, this.xCoord);
    }

    @JsonIgnore
    public long getLeftCoordinate() {
        return getAdjustedXCoordinate(CoordinateHelper.LEFT, this.xCoord);
    }

    @JsonIgnore
    public long getUpCoordinate() {
        return getAdjustedYCoordinate(CoordinateHelper.UP, this.yCoord);
    }

    @JsonIgnore
    public long getDownCoordinate() {
        return getAdjustedYCoordinate(CoordinateHelper.DOWN, this.yCoord);
    }

    @JsonIgnore
    public long getXCoordinateFromTile(long distance){
        return getXCoordinateFromTile(distance, this.getXCoord());
    }

    @JsonIgnore
    protected long getXCoordinateFromTile(long distance, long startingCoordinate){
        if(distance <= CoordinateHelper.LEFT){
            return getXCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedXCoordinate(CoordinateHelper.LEFT, startingCoordinate));
        } else if(distance >= CoordinateHelper.RIGHT){
            return getXCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedXCoordinate(CoordinateHelper.RIGHT, startingCoordinate));
        } else {
            return startingCoordinate;
        }
    }

    @JsonIgnore
    public long getYCoordinateFromTile(long distance){
        return getYCoordinateFromTile(distance, this.getYCoord());
    }

    @JsonIgnore
    protected long getYCoordinateFromTile(long distance, long startingCoordinate){
        if(distance <= CoordinateHelper.DOWN){
            return getYCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedYCoordinate(CoordinateHelper.DOWN, startingCoordinate));
        } else if(distance >= CoordinateHelper.UP){
            return getYCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedYCoordinate(CoordinateHelper.UP, startingCoordinate));
        } else {
            return startingCoordinate;
        }
    }

    @JsonIgnore
    public Coordinate calculateDistantCoordinate(int xVelocity, int yVelocity) {
        long newX = this.getXCoordinateFromTile(xVelocity);
        long newY = this.getYCoordinateFromTile(yVelocity);
        return world.getCoordinate(newX, newY);
    }

    protected long decreaseDistanceToZero(long distance){
        if(distance < 0){
            return distance+CoordinateHelper.RIGHT;
        } else if(distance > 0){
            return distance+CoordinateHelper.LEFT;
        } else {
            return 0;
        }
    }

    @JsonIgnore
    protected long getAdjustedXCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.RIGHT && startincCoordinate == this.world.getXSize()){
            return 1;
        } else if(adjustment <= CoordinateHelper.LEFT && startincCoordinate == 1){
            return this.world.getXSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    @JsonIgnore
    protected long getAdjustedYCoordinate(int adjustment, long startincCoordinate){
        if(adjustment >= CoordinateHelper.UP && startincCoordinate == this.world.getYSize()){
            return 1;
        } else if(adjustment <= CoordinateHelper.DOWN && startincCoordinate == 1){
            return this.world.getYSize();
        } else {
            return startincCoordinate+adjustment;
        }
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public List<Coordinate> getCoordinatesWithinRange(int range) {
        return getCoordinatesWithinRange(Collections.singletonList(this), range);
    }

    @JsonIgnore
    protected List<Coordinate> getCoordinatesWithinRange(List<Coordinate> coordinates, int range) {
        if(range>=1) {
            List<Coordinate> returnList = new ArrayList<>();
            coordinates.forEach(
                    coordinate -> returnList.addAll(coordinate.getNeighbours())
            );

            return getCoordinatesWithinRange(returnList, coordinates, range-1);
        } else {
            return coordinates;
        }
    }

    @JsonIgnore
    protected List<Coordinate> getCoordinatesWithinRange(List<Coordinate> coordinates, List<Coordinate> oldCoordinates, int range) {
        if(range>=1) {
            List<Coordinate> returnList = new ArrayList<>();
            coordinates.forEach(
                    coordinate -> {
                        if(!oldCoordinates.contains(coordinate)){
                            returnList.addAll(coordinate.getNeighbours());
                        }
                    }
            );

            return getCoordinatesWithinRange(returnList, coordinates, range-1);
        } else {
            return coordinates;
        }
    }

    public abstract Coordinate createBasicClone(World newWorld);

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
        String string = xCoord + "," + yCoord;
        return string.hashCode();
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                '}';
    }
}
