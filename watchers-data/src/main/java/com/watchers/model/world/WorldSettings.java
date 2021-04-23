package com.watchers.model.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@JsonSerialize
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "world_settings")
public class WorldSettings {

    @Id
    @JsonProperty("setting_id")
    @JsonView(Views.Internal.class)
    @SequenceGenerator(name = "World_Setting_Gen", sequenceName = "World_Settimg_Seq", allocationSize = 1)
    @GeneratedValue(generator = "World_Settimg_Seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
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
    private long heigtDivider;
    private int minimumContinents;
    private int maximumContinents;
    private int maxContinentSize;
    private int maxWidthLenghtBalance;

    // Erosion configuration
    private int minHeightDifference;
    private int maxErosion;

    // Climate configuration
    private long wetZone = 60;
    private long humidZone = 40;
    private long semiAridZone = 20;
    private long aridZone = 0;

    private long wetPrecipitation;
    private long humidPrecipitation;
    private long semiAridPrecipitation;
    private long aridPrecipitation;
    private long noPrecipitation;

    private int latitudinalStrength;
    private int longitudinalStrength;
}
