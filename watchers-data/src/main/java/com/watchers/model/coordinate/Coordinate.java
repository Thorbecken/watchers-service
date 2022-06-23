package com.watchers.model.coordinate;

import com.fasterxml.jackson.annotation.*;
import com.watchers.helper.CoordinateHelper;
import com.watchers.model.actors.Actor;
import com.watchers.model.climate.Climate;
import com.watchers.model.common.Views;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

@Data
@Entity
@NoArgsConstructor
@Table(name = "coordinate")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "coordinate_type", discriminatorType = DiscriminatorType.STRING)
@SequenceGenerator(name = "Coordinate_Gen", sequenceName = "Coordinate_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
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

    public static final BiPredicate<Coordinate, Coordinate> LOWER_OR_EQUAL_HEIGHT_PREDICATE = (x, y) -> y.getTile().getHeight() <= x.getTile().getHeight();

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("coordinateId")
    @GeneratedValue(generator = "Coordinate_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "coordinate_id", nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
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
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Tile tile;

    @JsonProperty("actors")
    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Actor> actors = new HashSet<>();

    @JsonView(Views.Public.class)
    @JsonIgnoreProperties({"world", "coordinates", "type"})
    @ManyToOne(fetch = FetchType.EAGER)
    private Continent continent;

    @JsonView(Views.Public.class)
    @JsonProperty("climate")
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "coordinate", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public void changeContinent(Continent newContinent) {
        if (continent != null) {
            continent.removeCoordinate(this);
        }
        this.continent = newContinent;
        if (newContinent != null) {
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
    public Coordinate getRightNeighbour() {
        long rightCoordinate = getRightCoordinate();
        return world.getCoordinate(rightCoordinate, this.getYCoord());
    }

    @JsonIgnore
    public Coordinate getLeftNeighbour() {
        long leftCoordinate = getLeftCoordinate();
        return world.getCoordinate(leftCoordinate, this.getYCoord());
    }

    @JsonIgnore
    public Coordinate getUpNeighbour() {
        long upCoordinate = getUpCoordinate();
        return world.getCoordinate(this.getXCoord(), upCoordinate);
    }

    @JsonIgnore
    public Coordinate getDownNeighbour() {
        long downCoordinate = getDownCoordinate();
        return world.getCoordinate(this.getXCoord(), downCoordinate);
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
    public long getXCoordinateFromTile(long distance) {
        return getXCoordinateFromTile(distance, this.getXCoord());
    }

    @JsonIgnore
    protected long getXCoordinateFromTile(long distance, long startingCoordinate) {
        if (distance <= CoordinateHelper.LEFT) {
            return getXCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedXCoordinate(CoordinateHelper.LEFT, startingCoordinate));
        } else if (distance >= CoordinateHelper.RIGHT) {
            return getXCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedXCoordinate(CoordinateHelper.RIGHT, startingCoordinate));
        } else {
            return startingCoordinate;
        }
    }

    @JsonIgnore
    public long getYCoordinateFromTile(long distance) {
        return getYCoordinateFromTile(distance, this.getYCoord());
    }

    @JsonIgnore
    protected long getYCoordinateFromTile(long distance, long startingCoordinate) {
        if (distance <= CoordinateHelper.DOWN) {
            return getYCoordinateFromTile(decreaseDistanceToZero(distance), getAdjustedYCoordinate(CoordinateHelper.DOWN, startingCoordinate));
        } else if (distance >= CoordinateHelper.UP) {
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

    protected long decreaseDistanceToZero(long distance) {
        if (distance < 0) {
            return distance + CoordinateHelper.RIGHT;
        } else if (distance > 0) {
            return distance + CoordinateHelper.LEFT;
        } else {
            return 0;
        }
    }

    @JsonIgnore
    protected long getAdjustedXCoordinate(int adjustment, long startincCoordinate) {
        if (adjustment >= CoordinateHelper.RIGHT && startincCoordinate == this.world.getXSize()) {
            return 1;
        } else if (adjustment <= CoordinateHelper.LEFT && startincCoordinate == 1) {
            return this.world.getXSize();
        } else {
            return startincCoordinate + adjustment;
        }
    }

    @JsonIgnore
    protected long getAdjustedYCoordinate(int adjustment, long startincCoordinate) {
        if (adjustment >= CoordinateHelper.UP && startincCoordinate == this.world.getYSize()) {
            return 1;
        } else if (adjustment <= CoordinateHelper.DOWN && startincCoordinate == 1) {
            return this.world.getYSize();
        } else {
            return startincCoordinate + adjustment;
        }
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public Set<Coordinate> getCoordinatesWithinRange(int range) {
        BiPredicate<Coordinate, Coordinate> predicate = (x, y) -> true;
        Set<Coordinate> returnList = getCoordinatesWithinRangeWithQualifier(new HashSet<>(Collections.singletonList(this)), range, predicate);
        returnList.remove(this);
        return returnList;
    }

    @JsonIgnore
    @SuppressWarnings("unused")
    public Set<Coordinate> getLowerOrEqualHeightLandCoordinatesWithinRange(int range) {
        Set<Coordinate> returnList = getCoordinatesWithinRangeWithQualifier(new HashSet<>(Collections.singletonList(this)),
                range, LOWER_OR_EQUAL_HEIGHT_PREDICATE);
        returnList.removeIf(Coordinate::isWater);
        returnList.remove(this);
        return returnList;
    }

    @JsonIgnore
    protected Set<Coordinate> getCoordinatesWithinRangeWithQualifier(int range, BiPredicate<Coordinate, Coordinate> predicate) {
        Set<Coordinate> returnList = getCoordinatesWithinRangeWithQualifier(new HashSet<>(Collections.singletonList(this)),
                range, predicate);
        returnList.remove(this);
        return returnList;
    }

    @JsonIgnore
    protected Set<Coordinate> getCoordinatesWithinRangeWithQualifier(Set<Coordinate> coordinates,
                                                                     int range, BiPredicate<Coordinate, Coordinate> predicate) {
        if (range >= 1) {
            Set<Coordinate> returnList = new HashSet<>();
            coordinates.stream()
                    .flatMap(coordinate -> coordinate.getNeighbours()
                            .stream()
                            .filter(neighbour -> predicate.test(coordinate, neighbour)))
                    .filter(neighbouringCoordinate -> !coordinates.contains(neighbouringCoordinate))
                    .forEach(returnList::add);
            returnList.addAll(coordinates);

            return getCoordinatesWithinRangeWithQualifier(returnList, range - 1, predicate);
        } else {
            return coordinates;
        }
    }

    public abstract Coordinate createClone(World newWorld);

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
                ", actors=" + actors.size() +
                '}';
    }

    @JsonIgnore
    public boolean isWater() {
        return tile.isWater();
    }
}
