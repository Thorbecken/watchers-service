package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.watchers.model.dto.MockTile;
import com.watchers.model.ParallelTask;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "biome")
@NoArgsConstructor
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
    @Column(name = "tile_id")
    @OneToOne(optional =  false)
    private Tile tile;

    public Biome(float currentFood, float maxFood, float fertility, Tile tile){
        this.currentFood = currentFood;
        this.maxFood = maxFood;
        this.fertility = fertility;
        this.tile = tile;
    }

    @Override
    public void processParallelTask() {
        if((currentFood+fertility) > maxFood){
            currentFood = maxFood;
        } else {
            currentFood = currentFood + fertility;
        }
    }

    public void addCurrentFood(float currentFood) {
        if(currentFood + this.currentFood > maxFood){
            this.currentFood = maxFood;
        } else {
            this.currentFood = this.currentFood + currentFood;
        }
    }

    @Override
    public String toString() {
        return "Biome{" +
                "currentFood=" + currentFood +
                ", maxFood=" + maxFood +
                ", fertility=" + fertility +
                '}';
    }

    public void clear() {
        this.currentFood = 0;
    }

    public void transferData(MockTile mockTile) {
        mockTile.setFood(mockTile.getFood() + currentFood);
    }

    public Biome createClone(Tile newTile) {
        Biome clone = new Biome();
        clone.setCurrentFood(this.currentFood);
        clone.setTile(newTile);
        clone.setId(this.id);
        clone.setMaxFood(this.maxFood);
        clone.setFertility(this.fertility);
        return clone;
    }
}
