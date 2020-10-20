package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Coordinate;
import com.watchers.model.common.Direction;
import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@JsonSerialize
@Table(name = "continent")
public class Continent {

    @Id
    @SequenceGenerator(name="Continent_Gen", sequenceName="Continent_Seq", allocationSize = 1)
    @GeneratedValue(generator="Continent_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "continent_id")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonIgnore
    @JsonProperty("coordinates")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "continent")
    private Set<Coordinate> coordinates = new HashSet<>();

    @JsonProperty("type")
    @Enumerated(value = EnumType.STRING)
    private SurfaceType type;

    @JsonProperty("direction")
    @OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL, optional = false)
    private Direction direction;

    public Continent(World world, SurfaceType surfaceType){
        this.world = world;
        this.world.getContinents().add(this);
        this.type = surfaceType;
        this.assignNewDriftDirection(1,world);
    }

    @JsonCreator
    @SuppressWarnings("unused")
    private Continent(){}

    /**
     * @param driftVelocity the speed at which the new directions of the continent can be
     * @return the continent which direction has been changed on the x and y axis.
     * These can be possitive or negative (left, right, up down).
     */
    public Continent assignNewDriftDirection(int driftVelocity, World world){
        if(this.direction == null) {
            int xVelocity = RandomHelper.getRandomWithNegativeNumbers(driftVelocity);
            int yVelocity = RandomHelper.getRandomWithNegativeNumbers(driftVelocity);

            Direction direction = new Direction(xVelocity, yVelocity);
            this.setDirection(direction);
        } else {
            this.direction.setXVelocity(RandomHelper.getRandomWithNegativeNumbers(driftVelocity));
            this.direction.setYVelocity(RandomHelper.getRandomWithNegativeNumbers(driftVelocity));
            world.setLastContinentInFlux(this.getId());
        }

        return this;
    }

    public void addCoordinate(Coordinate coordinate){
        this.getCoordinates().add(coordinate);
        coordinate.setContinent(this);
    }

    public void removeCoordinate(Coordinate coordinate){
        boolean removed = this.getCoordinates().remove(coordinate);
        if(!removed){
            throw new RuntimeException("Coordinate " + coordinate + " was to be removed from continent " + this + " but was not present!");
        }
        coordinate.setContinent(null);
    }

    @Override
    public String toString() {
        return "Continent{" +
                "id=" + id +
                ", world=" + world.getId() +
                ", coordinates=" + coordinates.size() +
                ", type=" + type +
                ", direction=" + direction.toString() +
                '}';
    }

    public Continent createClone(World newWorld) {
        Continent clone = new Continent();
        clone.setId(this.id);
        clone.setType(this.type);
        clone.setWorld(newWorld);
        clone.setDirection(this.direction.createClone());
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Continent)) return false;

        Continent continent = (Continent) o;

        if (id != null ? !id.equals(continent.id) : continent.id != null) return false;
        return type == continent.type;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
