package com.watchers.model.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.enums.WorldStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "world_meta_data")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class WorldMetaData {

    @Id
    @JsonProperty("world_meta_id")
    @JsonView(Views.Internal.class)
    @SequenceGenerator(name = "World_Meta_Gen", sequenceName = "World_Meta_Seq", allocationSize = 1)
    @GeneratedValue(generator = "World_Meta_Seq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonProperty("xSize")
    @Column(name = "x_size")
    @JsonView(Views.Public.class)
    private Long xSize;

    @JsonProperty("ySize")
    @Column(name = "y_size")
    @JsonView(Views.Public.class)
    private Long ySize;

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

    // save
    private long epoch;
    // continentaldrift
    private long era;
    // turn
    private long age;

    public WorldMetaData createClone(World world) {
        WorldMetaData clone = new WorldMetaData();
        clone.id = world.getId();
        clone.world = world;
        clone.xSize = world.getXSize();
        clone.ySize = world.getYSize();

        clone.worldStatusEnum = this.worldStatusEnum;
        clone.worldTypeEnum = this.worldTypeEnum;
        clone.needsProcessing = this.needsProcessing;
        clone.needsSaving = this.needsSaving;
        clone.needsContinentalShift = this.needsContinentalShift;

        clone.epoch = this.epoch;
        clone.age = this.age;
        clone.era = this.era;

        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldMetaData that = (WorldMetaData) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
