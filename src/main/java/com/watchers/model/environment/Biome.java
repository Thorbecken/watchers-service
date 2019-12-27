package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.ParallelTask;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "biome")
@SequenceGenerator(name="Biome_Gen", sequenceName="Biome_Seq", allocationSize = 1)
public class Biome implements ParallelTask {

    @Id
    @JsonIgnore
    @GeneratedValue(generator="Biome_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "biome_id", nullable = false)
    private Long id;

    private float currentFood;
    private float maxFood;
    private float fertility;

    @JsonIgnore
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "tile_id", nullable = false)
    private Tile tile;

    public Biome(float currentFood, float maxFood, float fertility){
        this.currentFood = currentFood;
        this.maxFood = maxFood;
        this.fertility = fertility;
    }

    @Override
    public void processParallelTask() {
        if((currentFood+fertility) > maxFood){
            currentFood = maxFood;
        } else {
            currentFood = currentFood + fertility;
        }
    }
}
