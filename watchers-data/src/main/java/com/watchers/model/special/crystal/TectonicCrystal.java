package com.watchers.model.special.crystal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.base.PointOfInterest;
import com.watchers.model.special.base.PointOfInterestType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Getter
@Setter
@Slf4j
@Entity
@JsonSerialize
@Table(name = "tectonic_crystal")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class TectonicCrystal extends PointOfInterest {

    @Id
    @JsonProperty("tectonicCrystalId")
    @Column(name = "tectonic_crystal_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "Tectonic_Crystal_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Tectonic_Crystal_Gen", sequenceName = "Tectonic_Crystal_Seq", allocationSize = 1)
    private Long id;

    @JsonProperty("timer")
    @Column(name = "timer")
    @JsonView(Views.Public.class)
    private int timer;

    private TectonicCrystal() {
    }

    public TectonicCrystal(Coordinate coordinate, int numberOfTurnsBeforeReallocation) {
        setCoordinate(coordinate);
        setPointOfInterestType(PointOfInterestType.TECTONIC_CRYSTAL);
        setTimer(numberOfTurnsBeforeReallocation);
    }

    @Override
    public String getDescription() {
        return "A crystal that pulls continents towards it location.";
    }

    @Override
    public PointOfInterest createClone(Coordinate coordinate, Tile tile) {
        TectonicCrystal clone = new TectonicCrystal(coordinate, this.timer);
        clone.setId(this.getId());
        clone.setCoordinate(coordinate);
        clone.setEarthBound(this.isEarthBound());
        clone.setPointOfInterestType(this.getPointOfInterestType());

        return clone;
    }
}
