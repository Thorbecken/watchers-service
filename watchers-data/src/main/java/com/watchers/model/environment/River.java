package com.watchers.model.environment;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.world.World;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.*;

@Data
@Slf4j
@Entity
@JsonSerialize
@NoArgsConstructor
@Table(name = "river")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class River {

    @Id
    @JsonProperty("riverId")
    @Column(name = "river_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "River_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "River_Gen", sequenceName = "River_Seq", allocationSize = 1)
    private Long id;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    private Tile tile;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "watershed_id", nullable = false)
    private Watershed watershed;

    //self join
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "downCurrentRiver", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<River> upCurrentRivers = new HashSet<>();

    //self join
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private River downCurrentRiver;

    @EqualsAndHashCode.Exclude
    @JsonView(Views.Public.class)
    @JsonProperty("riverThroughput")
    @Column(name = "riverThroughput")
    private long riverThroughput;

    private boolean riverEnd;

    public River(Watershed watershed) {
        this.watershed = watershed;
        watershed.addRiver(this);
    }

    public void setWatershed(Watershed watershed) {
        this.watershed = watershed;
        if (!watershed.getRiverFlow().contains(this)) {
            watershed.addRiver(this);
        }
    }

    public void checkIntegrity() {
        this.fixIfDownCurrentRiverIsNoLongerNeighbour();
    }

    public void fixIfDownCurrentRiverIsNoLongerNeighbour() {
        boolean downRiverIsNoLongerNeighbour = this.downCurrentRiver != null &&
                this.tile.getCoordinate().getNeighbours().stream()
                        .noneMatch(neighbouringCoordinate -> neighbouringCoordinate.equals(this.downCurrentRiver.getTile().getCoordinate()));
        if (downRiverIsNoLongerNeighbour) {
            this.fixRiver();
        }
    }

    private void fixRiver() {
        River oldDownCurrentRiver = this.getDownCurrentRiver();
        oldDownCurrentRiver.removeRiver();
        this.makeRiverFlowTillEnd();
    }

    public void makeRiverFlowTillEnd() {
        this.makeRiverFlowTillEnd(new ArrayList<>());
    }

    public void makeRiverFlowTillEnd(List<Coordinate> coordinates) {
        Tile currentTile = this.getTile();
        Coordinate currentCoordinate = currentTile.getCoordinate();
        if (coordinates.contains(currentCoordinate)) {
            log.warn("Loop detected while making the river flow till the end!");
            this.createLakeEnding(currentTile);
        }
        coordinates.add(currentCoordinate);
        Set<Coordinate> lowerOrEqualHeightCoordinates = currentCoordinate.getLowerOrEqualHeightLandCoordinatesWithinRange(1);
        coordinates.forEach(lowerOrEqualHeightCoordinates::remove);

        if (lowerOrEqualHeightCoordinates.size() == 0) {
            log.warn("River had nowhere to go!");
            this.createLakeEnding(currentTile);
        } else {
            Tile lowestTile = lowerOrEqualHeightCoordinates.stream()
                    .map(Coordinate::getTile)
                    .min(Comparator.comparing(Tile::getHeight))
                    .get();
            if (lowestTile.getRiver() == null) {
                lowestTile.setRiver(new River(this.getWatershed()));
                lowestTile.getRiver().getUpCurrentRivers().add(this);
                this.setDownCurrentRiver(lowestTile.getRiver());
                log.warn("Flowing river down current.");
                lowestTile.getRiver().makeRiverFlowTillEnd(coordinates);
            } else if (lowestTile.getWatershed() == null) {
                throw new RuntimeException("River with no Watershed was found!");
            } else if (lowestTile.getWatershed() != this.getWatershed()) {
                lowestTile.getRiver().getUpCurrentRivers().add(this);
                this.setDownCurrentRiver(lowestTile.getRiver());
                if (lowestTile.getWatershed() != this.getWatershed()) {
                    this.changeWatershedUpstream(lowestTile.getWatershed());
                }
            } else {
                log.warn("River was found looping in itself. Setting riverEnd to true. Waiting for RiverComputator to clean this up.");
                riverEnd = true;
            }
        }
    }

    private void createLakeEnding(Tile currentTile) {
        this.setRiverEnd(true);
        currentTile.setSurfaceType(SurfaceType.LAKE);
        this.createNewWatershed(this);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void createNewWatershed(River river) {
        World world = river.getTile().getCoordinate().getWorld();
        Watershed watershed = new Watershed(world);
        river.setWatershed(watershed);
        river.getUpCurrentRivers()
                .forEach(upstreamRiver -> upstreamRiver.changeWatershedUpstream(watershed));
    }

    private void changeWatershedUpstream(Watershed watershed) {
        List<River> riversNeedingToChangeWatershed = new ArrayList<>();
        List<River> riversChanged = new ArrayList<>();
        riversNeedingToChangeWatershed.add(this);

        int safetyToken = 0;
        while (!riversNeedingToChangeWatershed.isEmpty()) {
            safetyToken++;
            if (safetyToken > 2000) {
                log.warn("ChangeWatershedUpstream in loop for " + safetyToken + " loops");
            }

            River riverToBeChanged = riversNeedingToChangeWatershed.get(0);
            riversNeedingToChangeWatershed.remove(riverToBeChanged);
            riversChanged.add(riverToBeChanged);

            riverToBeChanged.setWatershed(watershed);

            Set<River> upCurrentRivers = this.getUpCurrentRivers();
            riversChanged.forEach(upCurrentRivers::remove);
            riversNeedingToChangeWatershed.addAll(upCurrentRivers);
        }
    }

    public void mergeRivers(River remainingRiver, River lostRiver) {
        Set<River> upstreamRivers = lostRiver.getUpCurrentRivers();
        lostRiver.removeRiver();

        upstreamRivers.forEach(upstreamRiver -> {
            upstreamRiver.setDownCurrentRiver(remainingRiver);
            upstreamRiver.changeWatershedUpstream(remainingRiver.getWatershed());
        });
    }

    private void removeRiver() {
        if (this.getDownCurrentRiver() != null) {
            this.getDownCurrentRiver().getUpCurrentRivers().remove(this);
        }
        if (this.getUpCurrentRivers() != null) {
            this.getUpCurrentRivers().remove(this);
            this.getUpCurrentRivers().forEach(upCurrentRiver -> upCurrentRiver.setDownCurrentRiver(null));
            this.getUpCurrentRivers().clear();
        }
    }

    @Override
    public String toString() {
        return "River{" +
                "id=" + id +
                ", riverThroughput=" + riverThroughput +
                ", riverEnd=" + riverEnd +
                '}';
    }

    public River createClone(Tile newTile) {
        River clone = new River();
        clone.setTile(newTile);
        clone.setId(this.id);
        clone.setWatershed(this.watershed);
        clone.setRiverEnd(this.riverEnd);
        clone.setRiverThroughput(this.riverThroughput);
        clone.setDownCurrentRiver(this.downCurrentRiver);
        if (this.upCurrentRivers.size() > 0) {
            clone.setUpCurrentRivers(new HashSet<>(this.upCurrentRivers));
        }
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        River river = (River) o;
        return Objects.equals(id, river.id)
                && Objects.equals(tile, river.tile)
                && watershed.equals(river.watershed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tile, watershed);
    }
}
