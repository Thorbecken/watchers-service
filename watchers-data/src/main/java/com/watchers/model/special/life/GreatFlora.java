package com.watchers.model.special.life;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.FloraTypeEnum;
import com.watchers.model.environment.Biome;
import com.watchers.model.environment.Flora;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.base.PointOfInterest;
import com.watchers.model.special.base.PointOfInterestType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.*;

@Getter
@Setter
@Slf4j
@Entity
@JsonSerialize
@Table(name = "great_flora")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class GreatFlora extends PointOfInterest {

    @Id
    @JsonProperty("greatFloraId")
    @Column(name = "great_flora_id")
    @JsonView(Views.Public.class)
    @GeneratedValue(generator = "Great_Flora_Gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "Great_Flora_Gen", sequenceName = "Great_Flora_Seq", allocationSize = 1)
    private Long id;

    @JsonView(Views.Public.class)
    @Column(name = "flora")
    @JsonProperty("flora")
    private Flora flora;

    private GreatFlora() {
    }

    public GreatFlora(Coordinate coordinate) {
        double meanTemperatur = coordinate.getClimate().getMeanTemperature();
        Flora floraType = Flora.getBasicTreeFlora(meanTemperatur);

        setTile(coordinate.getTile());
        setFlora(floraType);
        setPointOfInterestType(PointOfInterestType.GREAT_FLORA);
    }

    public GreatFlora(Tile tile, Flora flora) {
        setTile(tile);
        setFlora(flora);
        setPointOfInterestType(PointOfInterestType.GREAT_FLORA);
    }
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public PointOfInterest createClone(Coordinate coordinate, Tile tile) {
        GreatFlora clone = new GreatFlora();
        clone.setId(this.getId());
        clone.setFlora(this.getFlora());
        clone.setTile(tile);
        clone.setEarthBound(this.isEarthBound());
        clone.setPointOfInterestType(this.getPointOfInterestType());

        return clone;
    }

    public void seedLife(){
        Biome biome = getTile().getBiome();
        if(FloraTypeEnum.TREE.equals(flora.getType())){
            if(biome.hasOpenTreeSpot()){
                biome.setTreeFlora(flora);
            }
        }
        if(biome.hasOpenGrassSpot()){
            biome.setGrassFlora(Flora.GRASS);
        }
    }
}
