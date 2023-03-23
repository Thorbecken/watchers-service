package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.dto.MockTile;
import com.watchers.model.enums.FloraTypeEnum;
import com.watchers.model.interfaces.ParallelTask;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
@Table(name = "biome")
@NoArgsConstructor
@SequenceGenerator(name="Biome_Gen", sequenceName="Biome_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Biome implements ParallelTask {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("biomeId")
    @GeneratedValue(generator="Biome_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "biome_id", nullable = false)
    private Long id;

    @JsonView(Views.Public.class)
    @Column(name = "grassBiomass")
    @JsonProperty("grass_biomass")
    private double grassBiomass;

    @JsonView(Views.Public.class)
    @Column(name = "grassFlora")
    @JsonProperty("grass_flora")
    private Flora grassFlora;

    @JsonView(Views.Public.class)
    @Column(name = "treeBiomass")
    @JsonProperty("tree_Biomass")
    private double treeBiomass;

    @JsonView(Views.Public.class)
    @Column(name = "treeFlora")
    @JsonProperty("tree_flora")
    private Flora treeFlora;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "tile_id", nullable = false)
    private Tile tile;

    public void setGrassFlora(Flora grassFlora) {
        if(grassFlora != null) {
            if (FloraTypeEnum.GRASS.equals(grassFlora.getType())) {
                this.grassFlora = grassFlora;
                this.grassBiomass = 1;
            } else {
                throw new UnsupportedOperationException("GrassFlora needs to be of type Grass");
            }
        }
    }

    public void setTreeFlora(Flora treeFlora) {
        if(treeFlora != null) {
            if (FloraTypeEnum.TREE.equals(treeFlora.getType())) {
                this.treeFlora = treeFlora;
                this.treeBiomass = 1;
            } else {
                throw new UnsupportedOperationException("TreeFlora needs to be of type Tree");
            }
        }
    }

    public Biome(Tile tile){
        this.tile = tile;
    }

    @JsonIgnore
    public boolean hasOpenFloraSpots(){
        return grassFlora == null || treeFlora == null;
    }

    private void fillOpenFloraSpots(Flora neigbouringGrassFlora, Flora neighbouringTreeFlora){
        double temperature = this.getTile().getCoordinate().getClimate().getMeanTemperature();
        if(neigbouringGrassFlora != null
                && this.grassFlora == null
                && neigbouringGrassFlora.getNaturalHabitat().movableSurfaces.contains(tile.getSurfaceType())
                && neigbouringGrassFlora.getMinTemperature() <= temperature
                && neigbouringGrassFlora.getMaxTemperature() >= temperature){
            setGrassFlora(neigbouringGrassFlora);
            grow(grassFlora);
        }
        if(neighbouringTreeFlora != null
                && this.treeFlora == null
                && neighbouringTreeFlora.getNaturalHabitat().movableSurfaces.contains(tile.getSurfaceType())
                && neighbouringTreeFlora.getMinTemperature() <= temperature
                && neighbouringTreeFlora.getMaxTemperature() >= temperature){
            setTreeFlora(neighbouringTreeFlora);
            grow(treeFlora);
        }
    }

    @Override
    public void processParallelTask() {
        grow(grassFlora);
        grow(treeFlora);
    }

    private void grow(Flora flora){
        if(flora != null) {
            double oldBiomass = FloraTypeEnum.GRASS.equals(flora.getType()) ? grassBiomass:treeBiomass;
            double newBiomass;
            double waterUsage = flora.getWaterIntake() * oldBiomass * flora.getGrowthRate();
            double landMoisture = this.tile.getLandMoisture();
            if(waterUsage < landMoisture) {
                this.tile.reduceLandMoisture(waterUsage);
                if ((oldBiomass * flora.getGrowthRate()) > flora.getMaxBiomass()) {
                    newBiomass = flora.getMaxBiomass();
                } else {
                    newBiomass = oldBiomass + (oldBiomass * flora.getGrowthRate());
                }
            } else {
                newBiomass = landMoisture / flora.getWaterIntake();
                this.tile.reduceLandMoisture(landMoisture);
                if(newBiomass <= 0d){
                    if(FloraTypeEnum.GRASS.equals(flora.getType())){
                        this.grassFlora = null;
                    } else {
                        this.treeFlora = null;
                    }
                }
            }

           if(FloraTypeEnum.GRASS.equals(flora.getType())){
               this.grassBiomass = newBiomass;
           } else {
               this.treeBiomass = newBiomass;
           }
        }
    }

    public void spread(){
        if(this.grassFlora != null || this.treeFlora != null){
            this.getTile().getNeighbours().stream()
                    .filter(tile -> tile.getLandMoisture() > 0)
                    .map(Tile::getBiome)
                    .filter(Biome::hasOpenFloraSpots)
                    .forEach(openBiome -> openBiome.fillOpenFloraSpots(this.grassFlora, this.treeFlora));
        }
    }

    @Override
    public String toString() {
        return "Biome{" +
                '}';
    }

    @JsonIgnore
    public double getCurrentFood(){
        return grassBiomass + treeBiomass;
    }

    public void clear() {
        this.grassBiomass = 0;
        this.treeBiomass = 0;
    }

    public void transferData(MockTile mockTile) {
        mockTile.setGrassBiomass(mockTile.getGrassBiomass() + grassBiomass);
        mockTile.setTreeBiomass(mockTile.getTreeBiomass() + treeBiomass);
        if(mockTile.getGrassFlora() == null){
            mockTile.setGrassFlora(grassFlora);
        }
        if (mockTile.getTreeFlora() == null){
            mockTile.setTreeFlora(treeFlora);
        }
    }

    public Biome createClone(Tile newTile) {
        Biome clone = new Biome();
        clone.setGrassBiomass(this.grassBiomass);
        clone.setGrassFlora(this.grassFlora);
        clone.setTreeBiomass(this.treeBiomass);
        clone.setTreeFlora(this.treeFlora);
        clone.setTile(newTile);
        clone.setId(this.id);
        return clone;
    }

    public void addGrassBiomass(double grassBiomass) {
        if(grassFlora != null) {
            if (grassBiomass + this.grassBiomass > this.grassFlora.getMaxBiomass()) {
                this.grassBiomass = this.grassFlora.getMaxBiomass();
            } else {
                this.grassBiomass += grassBiomass;
            }
        }
    }

    public void addTreeBiomass(double treeBiomass) {
        if (treeFlora != null) {
            if (treeBiomass + this.treeBiomass > this.treeFlora.getMaxBiomass()) {
                this.treeBiomass = this.treeFlora.getMaxBiomass();
            } else {
                this.treeBiomass += treeBiomass;
            }
        }
    }

    public void forage(double foragingAmount) {
        this.grassBiomass -= foragingAmount;
        if(this.grassBiomass <= 0){
            this.grassFlora = null;
            this.treeBiomass += this.grassBiomass;
            this.grassBiomass = 0; // no negative biomass
            if(this.treeBiomass <= 0){
                this.treeFlora = null;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biome biome = (Biome) o;
        return Double.compare(biome.grassBiomass, grassBiomass) == 0
                && Double.compare(biome.treeBiomass, treeBiomass) == 0
                && Objects.equals(id, biome.id)
                && grassFlora == biome.grassFlora
                && treeFlora == biome.treeFlora
                && tile.equals(biome.tile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, grassBiomass, grassFlora, treeBiomass, treeFlora, tile);
    }
}
