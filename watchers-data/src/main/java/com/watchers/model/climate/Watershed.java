package com.watchers.model.climate;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import com.watchers.model.world.World;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
@Entity
@JsonSerialize
@Table(name = "watershed")
public class Watershed {

    @Id
    @JsonProperty("watershedId")
    @Column(name = "watershed_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "Watershed_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Watershed_Gen", sequenceName = "Watershed_Seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "watershed", cascade=CascadeType.ALL)
    private Set<Climate> watershedClimates = new HashSet<>();

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "watershed", cascade=CascadeType.ALL)
    private Set<River> riverFlow = new HashSet<>();
}
