package com.watchers.model.climate;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.Set;

@Data
@Slf4j
@Entity
@JsonSerialize
@Table(name = "river")
public class River {

    @Id
    @JsonProperty("riverId")
    @Column(name = "river_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "River_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "River_Gen", sequenceName = "River_Seq", allocationSize = 1)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    private Climate climate;

    @JsonIgnore
    @JoinColumn(name = "watershed_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Watershed watershed;

    //self join
    @JsonIgnore
    @OneToMany(mappedBy = "downCurrentRiver", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private Set<River> upCurrentRivers;

    //self join
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private River downCurrentRiver;

    @JsonView(Views.Public.class)
    @JsonProperty("riverThroughput")
    @Column(name = "riverThroughput")
    private long riverThroughput;

    private boolean downCurrentRiverIsStillNeighbour(){
        return this.climate.getCoordinate().getNeighbours().stream()
                .anyMatch(neighbouringCoordinate -> neighbouringCoordinate.equals(this.downCurrentRiver.getClimate().getCoordinate()));
    }
}
