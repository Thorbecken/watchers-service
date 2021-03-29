package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.helper.ClimateHelper;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.enums.SurfaceType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "climate")
@NoArgsConstructor
@SequenceGenerator(name="Climate_Gen", sequenceName="Climate_Seq", allocationSize = 1)
public class Climate {

    @Id
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator="Climate_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "climate_id", nullable = false)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    private Coordinate coordinate;

    @JsonProperty("longitude")
    @Column(name = "longitude")
    @JsonView(Views.Public.class)
    private double longitude;

    @JsonProperty("latitude")
    @Column(name = "latitude")
    @JsonView(Views.Public.class)
    private double latitude;

    @JsonProperty("skyTile")
    @JsonView(Views.Public.class)
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "climate", cascade=CascadeType.ALL, orphanRemoval = true)
    private SkyTile skyTile;

    @JsonProperty("climateEnum")
    @Column(name = "climateEnum")
    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private ClimateEnum climateEnum;

    @JsonView(Views.Public.class)
    @JsonProperty("temperatureEnum")
    @Column(name = "temperatureEnum")
    @Enumerated(value = EnumType.STRING)
    private TemperatureEnum temperatureEnum;

    @JsonView(Views.Public.class)
    @JsonProperty("precipitationEnum")
    @Column(name = "precipitationEnum")
    @Enumerated(value = EnumType.STRING)
    private PrecipitationEnum precipitationEnum;

    public Climate(Coordinate coordinate){
        this.coordinate = coordinate;
        double wx = coordinate.getWorld().getXSize();
        double wy = coordinate.getWorld().getYSize();
        double x = coordinate.getXCoord();
        double y = coordinate.getYCoord();

        this.latitude = ClimateHelper.transformToLatitude(y, wy);
        this.longitude = ClimateHelper.transformToLongitude(x, wx);

        this.skyTile = new SkyTile(this);
    }

    @JsonIgnore
    public boolean isWater(){
        SurfaceType surfaceType = coordinate.getTile().getSurfaceType();
        return  surfaceType.equals(SurfaceType.OCEAN) || surfaceType.equals(SurfaceType.SEA) || surfaceType.equals(SurfaceType.COASTAL);
    }

    @JsonIgnore
    public boolean isLand(){
        return !isWater();
    }

    @Override
    public String toString() {
        return "Climate{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", climateEnum=" + climateEnum +
                ", temperatureEnum=" + temperatureEnum +
                ", precipitationEnum=" + precipitationEnum +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Climate)) return false;

        Climate climate = (Climate) o;

        if (Double.compare(climate.longitude, longitude) != 0) return false;
        return Double.compare(climate.latitude, latitude) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(longitude);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public Climate createClone(Coordinate coordinateClone) {
        Climate clone = new Climate();
        clone.setSkyTile(skyTile.createClone(clone));
        clone.setPrecipitationEnum(precipitationEnum);
        clone.setClimateEnum(climateEnum);
        clone.setTemperatureEnum(temperatureEnum);
        clone.setCoordinate(coordinateClone);
        clone.setLatitude(latitude);
        clone.setLongitude(longitude);
        clone.setPrecipitationEnum(precipitationEnum);
        return clone;
    }
}
