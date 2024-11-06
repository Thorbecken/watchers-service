package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.helper.ClimateHelper;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "climate")
@NoArgsConstructor
@SequenceGenerator(name="Climate_Gen", sequenceName="Climate_Seq", allocationSize = 1)
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class Climate {

    private static final double MAX_MEAN_TEMPERATURE = 29d;
    private static final double TEMPERATURE_DIFFERENCE_PER_LATITUDE = 0.58d;
    private static final double METERS_PER_KILOMETER = 1000d;
    private static final double TEMPERATURE_CHANGE_PER_KILOMETER = 6.5d;
    private static final double ZERO = 0d;
    private static final double MAXIMUM_PROCENT = 100;
    private static final double ZERO_CELSIUS_IN_KELVIN = 273.15d;
    private static final double GRAMS_PER_MOLE_OF_WATER = 18.01528d;
    private static final double MOLAR_GAS_CONSTANT = 0.0821;


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

    @JsonProperty("airMoisture")
    @Column(name = "air_moisture")
    @JsonView(Views.Public.class)
    // grams/milliliter of water per cubic meter
    private double airMoisture;

    @JsonProperty("maximalAirMoisture")
    @Column(name = "maximal_air_moisture")
    @JsonView(Views.Public.class)
    // maximal grams/milliliter of water per cubic meter
    private double maximalAirMoisture;

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private double airMoistureLossage;

    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "startingClimate", cascade=CascadeType.ALL)
    private Set<Aircurrent> outgoingAircurrents = new HashSet<>();

    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "endingClimate", cascade=CascadeType.ALL)
    private Set<Aircurrent> incomingAircurrents = new HashSet<>();

    @Transient
    private double incomingMoisture;

    @JsonProperty("solarTemperature")
    @Column(name = "solarTemperature")
    @JsonView(Views.Public.class)
    private double solarTemperature;

    @JsonProperty("altitudeAdjustedTemperature")
    @Column(name = "altitudeAdjustedTemperature")
    @JsonView(Views.Public.class)
    private double altitudeAdjustedTemperature;

    @JsonProperty("meanTemperature")
    @Column(name = "meanTemperature")
    @JsonView(Views.Public.class)
    private double meanTemperature;

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
        setBaseTemperatures(latitude);
    }

    // celcius
    // 29(max mean temperature Earth) - -23 (min mean temperature Earth) = 52 (max difference in mean temperature on Earth)
    // 52 - 90 = 0.58 (Assumed difference in mean temperature per degree of latitude)
    // baseTemp = 29 - 0.58*latitude

    protected void setBaseTemperatures(double latitude) {
        double latitudeDifferenceFromEquator = Math.abs(latitude);
        this.solarTemperature = MAX_MEAN_TEMPERATURE - (TEMPERATURE_DIFFERENCE_PER_LATITUDE * latitudeDifferenceFromEquator);
        this.altitudeAdjustedTemperature = this.solarTemperature;
        this.meanTemperature = this.solarTemperature;
    }

    // the temperature that is based on the latitude is adjusted for the height of the tile.
    // tile height in meters needs to be adjusted to Celsius change. (6.5 Celsius per 1000 meters)
    protected void adjustTemperatureForAltitude(double seaLevel){
        double height = this.getCoordinate().getTile().getHeight();
        double temperatureChange = (height - seaLevel) / METERS_PER_KILOMETER * TEMPERATURE_CHANGE_PER_KILOMETER;

        this.altitudeAdjustedTemperature = Math.min(this.solarTemperature, this.solarTemperature - temperatureChange);
        this.meanTemperature = this.altitudeAdjustedTemperature;
    }

    // function inspired by AI
    protected void calculateMaximumGramsOfWaterVaporPerSquareMeter(){
        double currentTemperatureInKelvin = ZERO_CELSIUS_IN_KELVIN + this.meanTemperature;
        // pws = 6.112×e(T−29.6517.67×(T−273.15))
        double pws = 6.112 * Math.exp((17.67 * (currentTemperatureInKelvin - 273.15)) / (currentTemperatureInKelvin - 29.65));
        //  (pws×100) / (R×T)
        this.maximalAirMoisture = (pws * 100) / (MOLAR_GAS_CONSTANT * currentTemperatureInKelvin) * 1000;
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

    public Climate createClone(Coordinate coordinateClone) {
        Climate clone = new Climate();
        clone.setId(coordinateClone.getId());
        clone.setCoordinate(coordinateClone);
        clone.setLatitude(latitude);
        clone.setLongitude(longitude);
        clone.setSolarTemperature(solarTemperature);
        clone.setAltitudeAdjustedTemperature(altitudeAdjustedTemperature);
        clone.setMeanTemperature(meanTemperature);
        clone.setAirMoistureLossage(this.airMoistureLossage);
        clone.setAirMoisture(this.airMoisture);
        getOutgoingAircurrents().forEach(aircurrent -> clone.getOutgoingAircurrents().add(aircurrent.createOutgoingClone(clone)));
        getIncomingAircurrents().forEach(aircurrent -> clone.getIncomingAircurrents().add(aircurrent.createIncommingClone(clone)));

        return clone;
    }

    public void restoreBaseTemperature(double seaLevel) {
        this.adjustTemperatureForAltitude(seaLevel);
        this.meanTemperature = this.altitudeAdjustedTemperature;
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
        Set<Aircurrent> aircurrents = this.getIncomingAircurrents();
        int incommingAirPressure = aircurrents.stream().mapToInt(Aircurrent::getCurrentStrength).sum();
        double averageTemperatureDifference = aircurrents.stream()
                .mapToDouble(aircurrent -> aircurrent.getHeatTransfer(this, incommingAirPressure))
                .sum();
        this.heatChange = averageTemperatureDifference / 2d; // 2 is a arbitrary number to deminish the transfer from air.
    }

    @JsonIgnore
    public Aircurrent getIncommingLongitudalAirflow() {
        return incomingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLongitudalAirflow() {
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getIncommingLatitudalAirflow() {
        return incomingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLatitudallAirflow() {
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    public void addIncommingMoisture(double incommingMoisture) {
        this.incomingMoisture = this.incomingMoisture + incommingMoisture;
    }

    public void processIncommingMoisture() {
        addAirMoisture(this.incomingMoisture);
        this.incomingMoisture = 0;
    }

    public void addAirMoisture(double extraAirmoisture) {
        if (extraAirmoisture > 0d) {
            this.setAirMoisture(Math.min(extraAirmoisture + this.getAirMoisture(), 100d));
        }
    }

    public void reduceAirMoisture(double airmoistureReduction) {
        if (airmoistureReduction > 0d) {
            if (this.getAirMoisture() - airmoistureReduction > 0d) {
                this.getCoordinate().getTile().setRainfall(airmoistureReduction);
                this.getCoordinate().getTile().setAvailableWater(airmoistureReduction);
                this.setAirMoisture(this.getAirMoisture() - airmoistureReduction);
            } else {
                this.getCoordinate().getTile().setRainfall(this.getAirMoisture());
                this.getCoordinate().getTile().setAvailableWater(this.getAirMoisture());
                this.setAirMoisture(0d);
            }
        }
    }

    public void calculateNewMoistureLevel() {
        reduceAirMoisture(airMoistureLossage);
    }

    public void moveClouds() {
        double diffider = this.outgoingAircurrents.stream()
                .mapToInt(Aircurrent::getCurrentStrength)
                .sum();
        if (diffider != 0) {
            double transfer = this.getAirMoisture() / diffider;
            this.setAirMoisture(this.getAirMoisture() - (transfer * diffider));

            outgoingAircurrents.forEach(aircurrent -> aircurrent.transfer(transfer));
        }
    }

    public void addAirMoistureLossage(double heightAmount) {
        this.airMoistureLossage = this.airMoistureLossage + heightAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Climate climate = (Climate) o;
        return Objects.equals(id, climate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
