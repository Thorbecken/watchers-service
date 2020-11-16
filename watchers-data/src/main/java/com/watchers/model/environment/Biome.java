package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.dto.MockTile;
import com.watchers.model.interfaces.ParallelTask;
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
    @JsonView(Views.Internal.class)
    @JsonProperty("biomeId")
    @GeneratedValue(generator="Biome_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "biome_id", nullable = false)
    private Long id;

    @JsonView(Views.Public.class)
    @Column(name = "currentFood")
    @JsonProperty("current_food")
    private float currentFood;

    @JsonView(Views.Public.class)
    @Column(name = "maxFood")
    @JsonProperty("maxFood")
    private float maxFood;

    @JsonView(Views.Public.class)
    @Column(name = "fertility")
    @JsonProperty("fertility")
    private float fertility;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "tile_id", nullable = false)
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
