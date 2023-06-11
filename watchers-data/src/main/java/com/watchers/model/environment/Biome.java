package com.watchers.model.environment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.climate.Climate;
import com.watchers.model.common.Views;
import com.watchers.model.dto.MockTile;
import com.watchers.model.enums.FloraTypeEnum;
import com.watchers.model.interfaces.ParallelTask;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

import static com.watchers.model.enums.NaturalHabitat.SALT_WATER;
import static com.watchers.model.enums.NaturalHabitat.TERRESTRIAL;

@Data
@Entity
@Table(name = "biome")
@NoArgsConstructor
@SequenceGenerator(name = "Biome_Gen", sequenceName = "Biome_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Biome implements ParallelTask {

    @Id
    @JsonView(Views.Internal.class)
    @JsonProperty("biomeId")
    @GeneratedValue(generator = "Biome_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "biome_id", nullable = false)
    private Long id;

    @JsonView(Views.Public.class)
    @Column(name = "grass_biomass")
    @JsonProperty("grassBiomass")
    private double grassBiomass;

    @JsonView(Views.Public.class)
    @Column(name = "grass_flora")
    @JsonProperty("grassFlora")
    private Flora grassFlora;

    @JsonView(Views.Public.class)
    @Column(name = "tree_biomass")
    @JsonProperty("treeBiomass")
    private double treeBiomass;

    @JsonView(Views.Public.class)
    @Column(name = "tree_flora")
    @JsonProperty("treeFlora")
    private Flora treeFlora;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "tile_id", nullable = false)
    private Tile tile;

    public void setGrassFlora(Flora grassFlora) {
        if (grassFlora != null) {
            if (FloraTypeEnum.GRASS.equals(grassFlora.getType())) {
                this.grassFlora = grassFlora;
                this.grassBiomass = 1;
            } else {
                throw new UnsupportedOperationException("GrassFlora needs to be of type Grass");
            }
        }
    }

    public void setTreeFlora(Flora treeFlora) {
        if (treeFlora != null) {
            if (FloraTypeEnum.TREE.equals(treeFlora.getType())) {
                this.treeFlora = treeFlora;
                this.treeBiomass = 1;
            } else {
                throw new UnsupportedOperationException("TreeFlora needs to be of type Tree");
            }
        }
    }

    public Biome(Tile tile) {
        this.tile = tile;
        this.id = tile.getId();
    }

    @JsonIgnore
    public boolean hasOpenFloraSpots() {
        return grassFlora == null || treeFlora == null;
    }

    @JsonIgnore
    public boolean hasOpenGrassSpot() {
        return grassFlora == null;
    }

    @JsonIgnore
    public boolean hasOpenTreeSpot() {
        return treeFlora == null;
    }

    private void fillOpenGrassSpots(Flora neigbouringGrassFlora) {
        double temperature = this.getTile().getCoordinate().getClimate().getMeanTemperature();
        if (neigbouringGrassFlora != null
                && this.grassFlora == null
                && neigbouringGrassFlora.getNaturalHabitat().movableSurfaces.contains(tile.getSurfaceType())
                && neigbouringGrassFlora.getMinTemperature() <= temperature
                && neigbouringGrassFlora.getMaxTemperature() >= temperature) {
            setGrassFlora(neigbouringGrassFlora);
            grow(grassFlora);
        }
    }

    private void fillOpenTreeSpots(Flora neighbouringTreeFlora) {
        double temperature = this.getTile().getCoordinate().getClimate().getMeanTemperature();
        if (neighbouringTreeFlora != null
                && this.treeFlora == null
                && neighbouringTreeFlora.getNaturalHabitat().movableSurfaces.contains(tile.getSurfaceType())
                && neighbouringTreeFlora.getMinTemperature() <= temperature
                && neighbouringTreeFlora.getMaxTemperature() >= temperature) {
            setTreeFlora(neighbouringTreeFlora);
            grow(treeFlora);
        }
    }


    @Override
    public void processParallelTask() {
        grow(grassFlora);
        grow(treeFlora);
    }

    private void grow(Flora flora) {
        if (flora != null) {
            double oldBiomass = FloraTypeEnum.GRASS.equals(flora.getType()) ? grassBiomass : treeBiomass;
            double newBiomass;
            double waterUsage = flora.getWaterIntake() * oldBiomass * flora.getGrowthRate();
            double landMoisture = this.tile.getAvailableWater();
            if (waterUsage <= landMoisture) {
                this.tile.reduceLandMoisture(waterUsage);
                if ((oldBiomass * flora.getGrowthRate()) > flora.getMaxBiomass()) {
                    newBiomass = flora.getMaxBiomass();
                } else {
                    newBiomass = oldBiomass + (oldBiomass * flora.getGrowthRate());
                }
            } else {
                newBiomass = landMoisture / flora.getWaterIntake();
                this.tile.reduceLandMoisture(landMoisture);
                if (newBiomass <= 0d) {
                    if (FloraTypeEnum.GRASS.equals(flora.getType())) {
                        this.grassFlora = null;
                    } else {
                        this.treeFlora = null;
                    }
                }
            }

            if (FloraTypeEnum.GRASS.equals(flora.getType())) {
                this.grassBiomass = newBiomass;
            } else {
                this.treeBiomass = newBiomass;
            }
        }
    }

    public void spread() {
        if (this.grassFlora != null) {
            if (TERRESTRIAL.equals(this.grassFlora.getNaturalHabitat())) {
                this.getTile().getNeighbours().stream()
                        .filter(Tile::isLand)
                        .filter(tile -> tile.getRainfall() > 0)
                        .map(Tile::getBiome)
                        .filter(Biome::hasOpenGrassSpot)
                        .forEach(openBiome -> openBiome.fillOpenGrassSpots(this.grassFlora));
            } else if (SALT_WATER.equals(this.grassFlora.getNaturalHabitat())) {
                this.getTile().getNeighbours().stream()
                        .filter(Tile::isWater)
                        .map(Tile::getBiome)
                        .filter(Biome::hasOpenGrassSpot)
                        .forEach(openBiome -> openBiome.fillOpenGrassSpots(this.grassFlora));
            }
        }

        if (this.treeFlora != null) {
            if (TERRESTRIAL.equals(this.treeFlora.getNaturalHabitat())) {
                this.getTile().getNeighbours().stream()
                        .filter(Tile::isLand)
                        .filter(tile -> tile.getRainfall() > 0)
                        .map(Tile::getBiome)
                        .filter(Biome::hasOpenTreeSpot)
                        .forEach(openBiome -> openBiome.fillOpenTreeSpots(this.treeFlora));
            } else if (SALT_WATER.equals(this.treeFlora.getNaturalHabitat())) {
                this.getTile().getNeighbours().stream()
                        .filter(Tile::isWater)
                        .map(Tile::getBiome)
                        .filter(Biome::hasOpenTreeSpot)
                        .forEach(openBiome -> openBiome.fillOpenTreeSpots(this.treeFlora));
            }
        }
    }

    @JsonIgnore
    public double getCurrentFood() {
        return grassBiomass + treeBiomass;
    }

    public void clear() {
        this.grassBiomass = 0;
        this.treeBiomass = 0;
    }

    public void transferData(MockTile mockTile) {
        mockTile.setGrassBiomass(mockTile.getGrassBiomass() + grassBiomass);
        mockTile.setTreeBiomass(mockTile.getTreeBiomass() + treeBiomass);
        if (mockTile.getGrassFlora() == null) {
            mockTile.setGrassFlora(grassFlora);
        }
        if (mockTile.getTreeFlora() == null) {
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
        if (grassFlora != null) {
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
        if (this.grassBiomass <= 0) {
            this.grassFlora = null;
            this.treeBiomass += this.grassBiomass;
            this.grassBiomass = 0; // no negative biomass
            if (this.treeBiomass <= 0) {
                this.treeFlora = null;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biome biome = (Biome) o;
        return Objects.equals(id, biome.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void removeFlore() {
        this.grassBiomass = 0;
        this.grassFlora = null;

        this.treeBiomass = 0;
        this.treeFlora = null;
    }

    @Override
    public String toString() {
        return "Biome{" +
                "grassBiomass=" + grassBiomass +
                ", grassFlora=" + grassFlora +
                ", treeBiomass=" + treeBiomass +
                ", treeFlora=" + treeFlora +
                '}';
    }

    public void checkIntegrity() {
        Climate climate = this.getTile().getCoordinate().getClimate();
        if(treeFlora != null) {
            if (treeFlora.getMinTemperature() >= climate.getMeanTemperature()
                    || treeFlora.getMaxTemperature() < climate.getMeanTemperature()
                    || !treeFlora.getNaturalHabitat().movableSurfaces.contains(this.tile.getSurfaceType())) {
                this.treeFlora = null;
                this.treeBiomass = 0;
            }
        }
        if(grassFlora != null) {
            if (grassFlora.getMinTemperature() >= climate.getMeanTemperature()
                    || grassFlora.getMaxTemperature() < climate.getMeanTemperature()
                    || !grassFlora.getNaturalHabitat().movableSurfaces.contains(this.tile.getSurfaceType())) {
                this.grassFlora = null;
                this.grassBiomass = 0;
            }
        }
    }
}
