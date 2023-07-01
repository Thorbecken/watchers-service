    package com.watchers.model.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Objects;

    @Data
@Entity
@JsonSerialize
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "world_settings")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class WorldSettings {

    @Id
    @JsonProperty("setting_id")
    @JsonView(Views.Internal.class)
    @SequenceGenerator(name = "World_Setting_Gen", sequenceName = "World_Settimg_Seq", allocationSize = 1)
    @GeneratedValue(generator = "World_Settimg_Seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    // World configuration
    private long xSize;
    private long ySize;
    private int numberOfContinents;
    private boolean lifePreSeeded;
    private int coastalZone;
    private int oceanicZone;
    private int continentalToOcceanicRatio;
    private int continentalContinentWeight;

    // Continentalshift configuration
    private int driftVelocity;
    private int drifFlux;
    private int numberOfMantlePlumes;
    private long heigtDivider;
    private int minimumContinents;
    private int maximumContinents;
    private int maxContinentSize;
    private int maxWidthLenghtBalance;

    // Erosion configuration
    private int minHeightDifference;
    private int maxErosion;

    // Climate configuration
    private int latitudinalStrength;
    private int longitudinalStrength;

    public WorldSettings createClone(World world) {
        WorldSettings clone = new WorldSettings();

        clone.id = world.getId();
        clone.world = world;

        // World configuration
        clone.xSize = this.xSize;
        clone.ySize = this. ySize;
        clone.numberOfContinents = this.numberOfContinents;
        clone.lifePreSeeded = this.lifePreSeeded;
        clone.coastalZone = this.coastalZone;
        clone.oceanicZone = this.oceanicZone;
        clone.continentalToOcceanicRatio = this.continentalToOcceanicRatio;
        clone.continentalContinentWeight = this.continentalContinentWeight;

        // Continentalshift configuration
        clone.driftVelocity = this.driftVelocity;
        clone.drifFlux = this.drifFlux;
        clone.numberOfMantlePlumes = this.numberOfMantlePlumes;
        clone.heigtDivider = this.heigtDivider;
        clone.minimumContinents = this.minimumContinents;
        clone.maximumContinents = this.maximumContinents;
        clone.maxContinentSize = this.maxContinentSize;
        clone.maxWidthLenghtBalance = this.maxWidthLenghtBalance;

        // Erosion configuration
        clone.minHeightDifference = this.minHeightDifference;
        clone.maxErosion = this.maxErosion;

        // Climate configuration
        clone.latitudinalStrength = this.latitudinalStrength;
        clone.longitudinalStrength = this.longitudinalStrength;

        return clone;
    }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WorldSettings that = (WorldSettings) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
