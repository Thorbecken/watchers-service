package com.watchers.model.special.crystal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.base.PointOfInterest;
import com.watchers.model.special.life.GreatFlora;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;

@Getter
@Setter
@Slf4j
@Entity
@JsonSerialize
@Table(name = "aquifer_crystal")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class AquiferCrystal extends PointOfInterest {
    @Id
    @JsonProperty("aquiferCrystalId")
    @Column(name = "aquifer_crystal_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "Aquifer_Crystal_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Aquifer_Crystal_Gen", sequenceName = "Aquifer_Crystal_Seq", allocationSize = 1)
    private Long id;

    @Override
    public String getDescription() {
        return "Gives moisture to a given area";
    }

    @Override
    public PointOfInterest createClone(Coordinate coordinate, Tile tile) {
        AquiferCrystal clone = new AquiferCrystal();
        clone.setId(this.getId());
        clone.setTile(tile);
        clone.setEarthBound(this.isEarthBound());
        clone.setPointOfInterestType(this.getPointOfInterestType());

        return clone;
    }
}
