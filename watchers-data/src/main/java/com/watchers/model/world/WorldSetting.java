package com.watchers.model.world;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.enums.WorldStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class WorldSetting {

    @Id
    @JsonProperty("animalId")
    @Column(name = "world_id")
    @JsonView(Views.Internal.class)
    private Long worldId;

    @JsonProperty("worldStatus")
    @Column(name = "world_status")
    private WorldStatusEnum worldStatusEnum;

    @JsonProperty("worldTypeEnum")
    @JsonView(Views.Public.class)
    public WorldTypeEnum worldTypeEnum;

    @JsonProperty("needsProcessing")
    @Column(name = "needs_processing")
    private boolean needsProcessing;

    @JsonProperty("needsSaving")
    @Column(name = "needs_saving")
    private boolean needsSaving;

    @JsonProperty("needsContinentalShift")
    @Column(name = "needs_continental_shift")
    private boolean needsContinentalShift;

    @JsonProperty("heigtDivider")
    @Column(name = "height_divider")
    private long heigtDivider;

    @JsonProperty("minimumContinents")
    @Column(name = "minimum_continents")
    private int minimumContinents;
}
