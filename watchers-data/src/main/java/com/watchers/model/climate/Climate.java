package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.helper.ClimateHelper;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Table(name = "climate")
@NoArgsConstructor
@SequenceGenerator(name="Climate_Gen", sequenceName="Climate_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
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

    @JsonProperty("meanTemperature")
    @Column(name = "meanTemperature")
    @JsonView(Views.Public.class)
    private double meanTemperature;

    @JsonProperty("baseMeanTemperature")
    @Column(name = "baseMeanTemperature")
    @JsonView(Views.Public.class)
    private double baseMeanTemperature;

    @Transient
    private double heatChange;

    public Climate(Coordinate coordinate){
        this.coordinate = coordinate;
        double wx = coordinate.getWorld().getXSize();
        double wy = coordinate.getWorld().getYSize();
        double x = coordinate.getXCoord();
        double y = coordinate.getYCoord();

        this.latitude = ClimateHelper.transformToLatitude(y, wy);
        this.longitude = ClimateHelper.transformToLongitude(x, wx);
        this.baseMeanTemperature = calculateBaseMeanTemperature(this.latitude);
        this.meanTemperature = baseMeanTemperature;

        this.skyTile = new SkyTile(this);
    }

    // celcius
    // 29(max mean temperature Earth) - -23 (min mean temperature Earth) = 52 (max difference in mean temperature on Earth)
    // 52 - 90 = 0.58 (Assumed difference in mean temperature per degree of latitude)
    // baseTemp = 29 - 0.58*latitude
    protected double calculateBaseMeanTemperature(double latitude){
        return 29d - (0.58d * latitude);
    }

    @JsonIgnore
    public boolean isWater(){
        return coordinate.getTile().isWater();
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
        clone.setId(coordinateClone.getId());
        clone.setSkyTile(skyTile.createClone(clone));
        clone.setCoordinate(coordinateClone);
        clone.setLatitude(latitude);
        clone.setLongitude(longitude);
        clone.setBaseMeanTemperature(baseMeanTemperature);
        clone.setMeanTemperature(meanTemperature);
        return clone;
    }

    public void restoreBaseTemperature() {
        this.meanTemperature = this.baseMeanTemperature;
    }

    public void processHeatChange(){
        this.meanTemperature += this.heatChange;
    }

    public void transferWaterTemperature() {
        coordinate.getNeighbours().stream()
                .filter(Coordinate::isWater)
                .map(Coordinate::getClimate)
                .forEach(this::transferTemperatureThroughWater);
    }

    private void transferTemperatureThroughWater(Climate neighbouringClimate){
        double averageTemperature = (neighbouringClimate.meanTemperature + this.meanTemperature) / 2d;
        this.heatChange = (averageTemperature - this.meanTemperature) / 4d; // 4 is possible neighbours;
    }

    public void transferAirTemperature() {
        Set<Aircurrent> aircurrents = skyTile.getIncommingAircurrents();
        int incommingAirPressure = aircurrents.stream().mapToInt(Aircurrent::getCurrentStrength).sum();
        double averageTemperatureDifference = aircurrents.stream()
                .mapToDouble(aircurrent -> aircurrent.getHeatTransfer(this, incommingAirPressure))
                .sum();
        this.heatChange = averageTemperatureDifference / 2d; // 2 is a arbitrary number to deminish the transfer from air.
    }
}
