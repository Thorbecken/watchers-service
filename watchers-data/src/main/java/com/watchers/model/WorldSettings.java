package com.watchers.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class WorldSettings {

    @Id
    Long worldId;
    WorldStatusEnum worldStatusEnum;

    boolean needsProcessing;
    boolean needsSaving;
    boolean needsContinentalShift;

    private long heigtDivider;
    private int minimumContinents;

}
