package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.helper.ClimateHelper;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.world.World;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final double MOLAR_GAS_CONSTANT = 8.314d;
    protected static final double DIURNAL_TEMPERATURE_CHANGE_PER_DEGREE_OF_LATITUDE = 0.1d;
    private static final double HECTO_MULTIPLIER = 1000;
    private static final double MMHG_TO_KPA_MULTIPLIER = 0.133322;

    private static final Cache<Double, Double> WATER_VAPOR_CALCULATION_CACHE = CacheBuilder.newBuilder()
            .build();

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

    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "startingClimate", cascade=CascadeType.ALL)
    private Set<Aircurrent> outgoingAircurrents = new HashSet<>();

    @JsonView(Views.Public.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "endingClimate", cascade=CascadeType.ALL)
    private Set<Aircurrent> incomingAircurrents = new HashSet<>();

    @Transient
    private double incomingMoisture;

    @JsonProperty("solarTemperature")
    @Column(name = "solar_temperature")
    @JsonView(Views.Public.class)
    private double solarTemperature;

    @JsonProperty("adiabaticTemperatureAdjustment")
    @Column(name = "adiabatic_temperature_adjustment")
    @JsonView(Views.Public.class)
    private double adiabaticTemperatureAdjustment;

    @JsonProperty("meanTemperature")
    @Column(name = "mean_temperature")
    @JsonView(Views.Public.class)
    private double meanTemperature;

    @Transient
    private double temporaryMeanTemperature;

    @JsonProperty("dayTemperature")
    @Column(name = "day_temperature")
    @JsonView(Views.Public.class)
    private double dayTemperature;

    @JsonProperty("nightTemperature")
    @Column(name = "night_temperature")
    @JsonView(Views.Public.class)
    private double nightTemperature;

    @JsonProperty("maximalAirMoistureDay")
    @Column(name = "maximal_air_moisture_day")
    @JsonView(Views.Public.class)
    private double maximalAirMoistureDay;

    @JsonProperty("maximalAirMoistureNight")
    @Column(name = "maximal_air_moisture_night")
    @JsonView(Views.Public.class)
    private double maximalAirMoistureNight;

    public Climate(Coordinate coordinate){
        this.coordinate = coordinate;
        double wx = coordinate.getWorld().getXSize();
        double wy = coordinate.getWorld().getYSize();
        double x = coordinate.getXCoord();
        double y = coordinate.getYCoord();

        this.latitude = ClimateHelper.transformToLatitude(y, wy);
        this.longitude = ClimateHelper.transformToLongitude(x, wx);
        setBaseTemperatures();
    }

    // celcius
    // 29(max mean temperature Earth) - -23 (min mean temperature Earth) = 52 (max difference in mean temperature on Earth)
    // 52 - 90 = 0.58 (Assumed difference in mean temperature per degree of latitude)
    // baseTemp = 29 - 0.58*latitude
    protected void setBaseTemperatures() {
        double latitudeDifferenceFromEquator = Math.abs(this.latitude);
        this.solarTemperature = MAX_MEAN_TEMPERATURE - (TEMPERATURE_DIFFERENCE_PER_LATITUDE * latitudeDifferenceFromEquator);
        this.meanTemperature = this.solarTemperature;
        this.setDayNightTemperatures();
    }

    protected void setDayNightTemperatures() {
        double temperatureSwing = DIURNAL_TEMPERATURE_CHANGE_PER_DEGREE_OF_LATITUDE * Math.abs(latitude);
        this.nightTemperature = this.meanTemperature - temperatureSwing;
        this.dayTemperature = this.meanTemperature + temperatureSwing;

        this.setMeanDayAndNightMaximalAirMoister();
    }

    // the temperature that is based on the latitude is adjusted for the height of the tile.
    // tile height in meters needs to be adjusted to Celsius change. (6.5 Celsius per 1000 meters)
    protected void calculateAdjustedTemperatureForAltitude(double seaLevel){
        this.adiabaticTemperatureAdjustment = Math.min(-0d, (this.getCoordinate().getTile().getHeight() - seaLevel) / METERS_PER_KILOMETER * TEMPERATURE_CHANGE_PER_KILOMETER * -1);
    }

    static public void recalculateTemperatures(World world){
        List<Climate> climateList = world.getCoordinates().stream()
                .map(Coordinate::getClimate)
                .collect(Collectors.toList());
        double seaLevel = world.getSeaLevel();
        // reset temperature for adiabatic temperature adjustment.
        // as higher altitudes reduce temperature, so do lower altitudes increase temperature.
        // so the adiabatic temperature is tile specific.
        climateList.parallelStream().forEach(Climate::resetTemperature);
        // recalculate new base temperatures
        climateList.parallelStream().forEach(climate -> climate.calculateAdjustedTemperatureForAltitude(seaLevel));
        // calculate new mean temperatures
        climateList.parallelStream().forEach(Climate::calculateAndSetTemporaryMeanTemperature);
        // set new mean temperatures with adiabatic adjustment and adjust air moisture for new temperatures
        climateList.parallelStream().forEach(Climate::adjustTemperatureAndMoistureLevel);
    }

    protected void resetTemperature() {
        this.meanTemperature = this.meanTemperature - adiabaticTemperatureAdjustment;
        this.adiabaticTemperatureAdjustment = 0;
    }

    @SuppressWarnings("OptionalIsPresent")
    protected void calculateAndSetTemporaryMeanTemperature(){
        double incomingHeathFromAirCurrents = this.calculateIncomingHeathFromAirCurrents();
        if(this.isLand()) {
            temporaryMeanTemperature = (solarTemperature + incomingHeathFromAirCurrents) / 2;
        } else {
            Optional<Double> incomingHeathFromWaterCurrents = this.calculateIncomingHeathFromWaterCurrents();
            if (incomingHeathFromWaterCurrents.isPresent()) {
                temporaryMeanTemperature = (solarTemperature + incomingHeathFromAirCurrents + incomingHeathFromWaterCurrents.get()) / 3;
            } else {
                temporaryMeanTemperature = (solarTemperature + incomingHeathFromAirCurrents) / 2;
            }
        }
    }

    protected double calculateIncomingHeathFromAirCurrents(){
        double incomingHeathTotal = incomingAircurrents.stream()
                .mapToDouble(aircurrent -> aircurrent.getStartingClimate().getMeanTemperature() * aircurrent.getCurrentStrength())
                .sum();
        double totalIncomingAirCurrentStrength = incomingAircurrents.stream().mapToDouble(Aircurrent::getCurrentStrength).sum();
        return incomingHeathTotal / totalIncomingAirCurrentStrength;
    }

    protected Optional<Double> calculateIncomingHeathFromWaterCurrents(){
        List<Climate> waterNeighbours = coordinate.getNeighbours().stream()
                .filter(Coordinate::isWater)
                .map(Coordinate::getClimate)
                .collect(Collectors.toList());

        double incomingHeathTotal = waterNeighbours.stream()
                .mapToDouble(Climate::getMeanTemperature)
                .sum();
        return waterNeighbours.size() > 0 ? Optional.of(incomingHeathTotal / waterNeighbours.size()) : Optional.empty();
    }

    protected void adjustTemperatureAndMoistureLevel(){
        meanTemperature = temporaryMeanTemperature + adiabaticTemperatureAdjustment;
        this.setDayNightTemperatures();
        this.processRainfallAndCondensation();
    }

    private void setMeanDayAndNightMaximalAirMoister(){
        this.maximalAirMoisture = calculateMaximumGramsOfWaterVaporPerCubicMeter(meanTemperature);
        this.maximalAirMoistureDay = calculateMaximumGramsOfWaterVaporPerCubicMeter(dayTemperature);
        this.maximalAirMoistureNight = calculateMaximumGramsOfWaterVaporPerCubicMeter(nightTemperature);
    }

    protected double calculateMaximumGramsOfWaterVaporPerCubicMeter(double celsius) {
        Double cacheResult = WATER_VAPOR_CALCULATION_CACHE.getIfPresent(celsius);
        if(cacheResult != null){
            return cacheResult;
        } else {
            double kelvin = celsius + ZERO_CELSIUS_IN_KELVIN;
            double pwsInHPA = calculateSaturatedVaporPressure(celsius);
            double pwsInPA = pwsInHPA * HECTO_MULTIPLIER;

            double maximumGramsOfWaterVaporPerCubicMeter = calculateWaterVaporDensity(pwsInPA, kelvin);
            WATER_VAPOR_CALCULATION_CACHE.put(celsius, maximumGramsOfWaterVaporPerCubicMeter);
            return maximumGramsOfWaterVaporPerCubicMeter;
        }
    }

    protected double calculateSaturatedVaporPressure(double celsius) {
        // De formule is P = exp(20.386 - 5132 / T)
        double mmHg = Math.exp(20.386 - (5132 / (celsius + ZERO_CELSIUS_IN_KELVIN)));
        return mmHg * MMHG_TO_KPA_MULTIPLIER;
    }

    private double calculateWaterVaporDensity(double pressure, double temperature) {
        return (pressure * GRAMS_PER_MOLE_OF_WATER) / (MOLAR_GAS_CONSTANT * temperature);
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
        clone.setAdiabaticTemperatureAdjustment(adiabaticTemperatureAdjustment);
        clone.setMeanTemperature(meanTemperature);
        clone.setAirMoisture(this.airMoisture);
        getOutgoingAircurrents().forEach(aircurrent -> clone.getOutgoingAircurrents().add(aircurrent.createOutgoingClone(clone)));
        getIncomingAircurrents().forEach(aircurrent -> clone.getIncomingAircurrents().add(aircurrent.createIncommingClone(clone)));

        return clone;
    }

    @JsonIgnore
    public Aircurrent getIncomingLongitudeAirflow() {
        return incomingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLongitudinalAirflow() {
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LONGITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getIncomingLatitudeAirflow() {
        return incomingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    @JsonIgnore
    public Aircurrent getOutgoingLatitudinalAirflow() {
        return outgoingAircurrents.stream().filter(aircurrent -> AircurrentType
                .LATITUDAL.equals(aircurrent.getAircurrentType())).findFirst().orElseThrow();
    }

    public void addIncomingMoisture(double incomingMoisture) {
        this.incomingMoisture = this.incomingMoisture + incomingMoisture;
    }

    public void processIncomingMoisture() {
        addAirMoisture(this.incomingMoisture);
        this.incomingMoisture = 0;
    }

    public void addAirMoisture(double extraAirmoisture) {
        if (extraAirmoisture > 0d) {
            this.setAirMoisture(Math.min(extraAirmoisture + this.getAirMoisture(), this.maximalAirMoisture));
        }
    }

    public void processRainfallAndCondensation() {
        // temperature rainfall is the rainfall that occurs because of drop in temperature across distances.
        double temperatureRainfall = Math.max(0, (this.airMoisture - this.maximalAirMoisture));
        // diurnal rainfall is the rainfall that occurs because of the drop in temperature at night.
        double diurnalRainfall = Math.max(0, (this.airMoisture - temperatureRainfall - this.maximalAirMoistureNight));
        double totalRainfall = temperatureRainfall + diurnalRainfall;
        this.getCoordinate().getTile().setRainfall(totalRainfall);
        this.setAirMoisture(this.getAirMoisture() - totalRainfall);
    }

    public void moveClouds() {
        double divider = this.outgoingAircurrents.stream()
                .mapToInt(Aircurrent::getCurrentStrength)
                .sum();
        if (divider != 0) {
            double transferPerStrength = this.getAirMoisture() / divider;
            // below is for leftovers because of rounding down to ints.
            this.setAirMoisture(this.getAirMoisture() - (transferPerStrength * divider));
            outgoingAircurrents.forEach(aircurrent -> aircurrent.transfer(transferPerStrength));
        }
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
