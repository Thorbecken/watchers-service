package com.watchers.model.worldsetting;

import com.watchers.model.coordinate.WorldTypeEnum;
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
    @SequenceGenerator(name="World_Setting_Gen", sequenceName="World_Setting_Seq", allocationSize = 1)
    @GeneratedValue(generator="World_Setting_Gen", strategy = GenerationType.SEQUENCE)
    Long worldId;

    WorldStatusEnum worldStatusEnum;

    WorldTypeEnum worldTypeEnum;

    boolean needsProcessing;
    boolean needsSaving;
    boolean needsContinentalShift;

    private long heigtDivider;
    private int minimumContinents;
}
