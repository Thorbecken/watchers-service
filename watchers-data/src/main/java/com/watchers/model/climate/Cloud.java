package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "cloud")
@NoArgsConstructor
@SequenceGenerator(name="Cloud_Gen", sequenceName="Cloud_Seq", allocationSize = 1)
public class Cloud {

    @Id
    @JsonIgnore
    @GeneratedValue(generator="Cloud_Gen", strategy = GenerationType.SEQUENCE)
    private Long id;
    private int airMoisture;

    @Transient
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Climate incomingClimate;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.EAGER)
    private Climate currentClimate;

    public Cloud(Climate climate){
        this.setCurrentClimate(climate);
    }

    public void addAirMoisture(int extraAirmoisture){
        if(extraAirmoisture > 0) {
            if(extraAirmoisture+this.airMoisture < 100) {
                this.airMoisture = this.airMoisture + extraAirmoisture;
            } else{
                this.airMoisture = 100;
            }
        }
    }

    public void reduceAirMoisture(int airmoistureReduction){
        if(airmoistureReduction > 0) {
            if(this.airMoisture-airmoistureReduction > 0) {
                this.airMoisture = this.airMoisture - airmoistureReduction;
            } else {
                this.airMoisture = 0;
            }
        }
    }

    @Override
    public String toString() {
        return "Cloud{" +
                "id=" + id +
                ", airMoisture=" + airMoisture +
                '}';
    }
}
