package com.watchers.model.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "world_meta_data")
public class WorldMetaData {

    @Id
    @JsonProperty("meta_id")
    @JsonView(Views.Internal.class)
    @SequenceGenerator(name = "World_Meta_Gen", sequenceName = "World_Meta_Seq", allocationSize = 1)
    @GeneratedValue(generator = "World_Meta_Seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    private World world;

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

    public WorldMetaData createClone(World world) {
        WorldMetaData clone = new WorldMetaData();
        clone.id = world.getId();
        clone.world = world;

        clone.worldStatusEnum = this.worldStatusEnum;
        clone.worldTypeEnum = this.worldTypeEnum;
        clone.needsProcessing = this.needsProcessing;
        clone.needsSaving = this.needsSaving;
        clone.needsContinentalShift = this.needsContinentalShift;

        return clone;
    }
}
