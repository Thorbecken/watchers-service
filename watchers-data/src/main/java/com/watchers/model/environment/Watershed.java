package com.watchers.model.environment;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import com.watchers.model.world.World;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Slf4j
@Entity
@JsonSerialize
@NoArgsConstructor
@Table(name = "watershed")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Watershed {

    @Id
    @JsonProperty("watershedId")
    @Column(name = "watershed_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "Watershed_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Watershed_Gen", sequenceName = "Watershed_Seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "watershed")
    private List<Tile> watershedTiles = new ArrayList<>();

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "watershed", cascade=CascadeType.ALL)
    private List<River> riverFlow = new ArrayList<>();

    public Watershed(World world){
        world.addWatershed(this);
        Assert.notNull(world, "The world can't be null.");
    }

    public List<Tile> getWatershedTiles() {
        return new ArrayList<>(watershedTiles);
    }

    public void clearWatershedTiles() {
        watershedTiles.clear();
    }

    private void setWatershedTiles(List<Tile> watershedTiles) {
        this.watershedTiles = watershedTiles;
    }

    public void addTile(Tile tile) {
        watershedTiles.add(tile);
        tile.setWatershed(this);
    }

    public void removeTile(Tile tile) {
        boolean tileRemoved = watershedTiles.remove(tile);
        if(tileRemoved){
            tile.setWatershed(null);
        }
    }

    public List<River> getRiverFlow() {
        return new ArrayList<>(riverFlow);
    }

    private void setRiverFlow(List<River> riverFlow) {
        this.riverFlow = riverFlow;
    }

    public void clearRiverFlow() {
        riverFlow.clear();
    }

    public void clearInformation(){
        this.riverFlow = null;
        this.watershedTiles = null;
    }

    public void setInformation(List<River> riverSet, List<Tile> tileSet) {
        this.riverFlow = riverSet;
        this.watershedTiles = tileSet;
    }

    public void addRiver(River river){
        riverFlow.add(river);
        river.setWatershed(this);
    }

    @Override
    public String toString() {
        return "Watershed{" +
                "id=" + id +
                '}';
    }

    public Watershed createClone(World newWorld){
        Watershed clone = new Watershed();
        clone.setId(this.id);
        clone.setWorld(newWorld);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Watershed watershed = (Watershed) o;
        return Objects.equals(id, watershed.id)
                && world.equals(watershed.world)
                && watershedTiles.equals(watershed.watershedTiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, world, watershedTiles);
    }
}
