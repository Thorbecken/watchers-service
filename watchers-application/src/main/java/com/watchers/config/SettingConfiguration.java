package com.watchers.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class SettingConfiguration {

    private long heigtDivider;
    private int minimumContinents;

    public SettingConfiguration(
            @Value("${watch.heightdivider}") long heigtDivider,
            @Value("${watch.minContinents}") int minimumContinents){
        this.heigtDivider = heigtDivider;
        this.minimumContinents = minimumContinents;
    }
}
