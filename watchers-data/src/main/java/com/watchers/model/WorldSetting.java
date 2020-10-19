package com.watchers.model;

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
    Long worldId;
    WorldStatusEnum worldStatusEnum;

    boolean needsProcessing;
    boolean needsSaving;
    boolean needsContinentalShift;

    private long heigtDivider;
    private int minimumContinents;
}
