package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.helper.RandomHelper;
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
    @JsonIgnore
    @SequenceGenerator(name="Continent_Gen", sequenceName="Continent_Seq", allocationSize = 1)
    @GeneratedValue(generator="Continent_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "continent_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonIgnore
    @JsonProperty("tiles")
    @OneToMany(mappedBy = "continent", cascade=CascadeType.ALL)
    private Set<Tile> tiles;

    @JsonProperty("type")
    private SurfaceType type;

    @JsonProperty("direction")
    @OneToOne(fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    private Direction direction;

    public Continent(World world, SurfaceType surfaceType){
        this.tiles = new HashSet<>();
        this.world = world;
        this.type = surfaceType;
    }

    private Continent(){}

    /**
     * @param driftVelocity the speed at which the new directions of the continent can be
     * @return the continent which direction has been changed on the x and y axis.
     * These can be possitive or negative (left, right, up down).
     */
    public Continent assignNewDriftDirection(int driftVelocity){
        int xVelocity = RandomHelper.getRandomWithNegativeNumbers(driftVelocity);
        int yVelocity = RandomHelper.getRandomWithNegativeNumbers(driftVelocity);

        Direction direction = new Direction(xVelocity, yVelocity);
        this.setDirection(direction);
        this.getWorld().setLastContinentInFlux(this.getId());

        return this;
    }
}
