package com.watchers.model.special.crystal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.helper.RandomHelper;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.base.PointOfInterest;
import com.watchers.model.special.base.PointOfInterestType;
import com.watchers.model.special.life.GreatFlora;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Getter
@Setter
@Slf4j
@Entity
@JsonSerialize
@Table(name = "hot_spot_crystal")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class HotSpotCrystal extends PointOfInterest {

    @Id
    @JsonProperty("hotSpotCrystalId")
    @Column(name = "hot_spot_crystal_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "Hot_Spot_Crystal_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Hot_Spot_Crystal_Gen", sequenceName = "Hot_Spot_Crystal_Seq", allocationSize = 1)
    private Long id;

    @JsonProperty("timer")
    @Column(name = "timer")
    @JsonView(Views.Public.class)
    private long timer;

    @JsonProperty("heightBuildup")
    @Column(name = "height_buildup")
    @JsonView(Views.Public.class)
    private long heightBuildup;

    private HotSpotCrystal() {
    }

    public HotSpotCrystal(Coordinate coordinate) {
        setCoordinate(coordinate);
        setPointOfInterestType(PointOfInterestType.TECTONIC_CRYSTAL);
        setTimer(RandomHelper.getRandomNonZero(178));
    }

    @Override
    public String getDescription() {
        return "Takes all or some of the lost height during tectonic shifting and adds it to a single coordinate.";
    }

    @Override
    public PointOfInterest createClone(Coordinate coordinate, Tile tile) {
        HotSpotCrystal clone = new HotSpotCrystal();
        clone.setId(this.getId());
        clone.setTimer(this.getTimer());
        clone.setHeightBuildup(this.heightBuildup);
        clone.setCoordinate(coordinate);
        clone.setEarthBound(this.isEarthBound());
        clone.setPointOfInterestType(this.getPointOfInterestType());

        return clone;
    }

    public void addHeightBuildup(long extraHeight) {
        this.heightBuildup += extraHeight;
    }
}
