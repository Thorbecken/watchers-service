package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.SurfaceType;
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
    @JsonIgnore
    @GeneratedValue(generator="Climate_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "climate_id", nullable = false)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    private Coordinate coordinate;

    @JsonProperty("longitude")
    @Column(name = "longitude")
    private double longitude;

    @JsonProperty("latitude")
    @Column(name = "latitude")
    private double latitude;

    @JsonIgnore
    @Transient
    private Cloud incomingCloud;

    @JsonProperty("currentCloud")
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "currentClimate", cascade=CascadeType.ALL, orphanRemoval = false)
    private Cloud currentCloud;

    @JsonProperty("climateEnum")
    @Column(name = "climateEnum")
    @Enumerated(value = EnumType.STRING)
    private ClimateEnum climateEnum;

    @JsonProperty("temperatureEnum")
    @Column(name = "temperatureEnum")
    @Enumerated(value = EnumType.STRING)
    private TemperatureEnum temperatureEnum;

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

        this.latitude = (int)(y / wy * 180L) - 90L;
        this.longitude = (int)(x / wx * 360L);

        this.currentCloud = new Cloud(this);
    }

    public boolean isWater(){
        SurfaceType surfaceType = coordinate.getTile().getSurfaceType();
        return  surfaceType.equals(SurfaceType.OCEAN) || surfaceType.equals(SurfaceType.SEA) || surfaceType.equals(SurfaceType.COASTAL);
    }

    public boolean isLand(){
        return !isWater();
    }

    public void setIncomingCloud(Cloud incomingCloud) {
        this.incomingCloud = incomingCloud;
//        if(!this.equals(incomingCloud.getIncomingClimate())){
//            incomingCloud.setIncomingClimate(this);
//        }
    }

    public void setCurrentCloud(Cloud currentCloud) {
        this.currentCloud = currentCloud;
        currentCloud.setCurrentClimate(this);
    }

    @Override
    public String toString() {
        return "Climate{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", incomingCloud=" + incomingCloud +
                ", currentCloud=" + currentCloud +
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
}
