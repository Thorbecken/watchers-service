package com.watchers.model.special.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.AquiferCrystal;
import com.watchers.model.special.HotSpotCrystal;
import com.watchers.model.special.TectonicCrystal;
import com.watchers.model.special.WindCrystal;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "point_of_interest")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@DiscriminatorColumn(name = "point_of_interest_type", discriminatorType = DiscriminatorType.STRING)
@SequenceGenerator(name = "Point_Of_Interest_Gen", sequenceName = "Point_Of_Interest_Seq", allocationSize = 1)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HotSpotCrystal.class, name = "HotSpotCrystal"),
        @JsonSubTypes.Type(value = TectonicCrystal.class, name = "TectonicCrystal"),
        @JsonSubTypes.Type(value = AquiferCrystal.class, name = "AquiferCrystal"),
        @JsonSubTypes.Type(value = WindCrystal.class, name = "WindCrystal")
})
public abstract class PointOfInterest {

    @Id
    @JsonView(Views.Internal.class)
    @SequenceGenerator(name = "Point_Of_Interest_Gen", sequenceName = "Point_Of_Interest_Seq", allocationSize = 1)
    @GeneratedValue(generator = "Point_Of_Interest_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "point_of_interest_id")
    private Long id;

    @JsonView(Views.Public.class)
    @Enumerated(value = EnumType.STRING)
    private PointOfInterestType pointOfInterestType;

    @Column(name = "earth_bound")
    @JsonView(Views.Public.class)
    private boolean earthBound;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "tile_id", nullable = true)
    private Tile tile;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "coordinate_id", nullable = true)
    private Coordinate coordinate;

    public abstract String getDescription();

    public void setTile(Tile tile) {
        if(this.coordinate != null){
            this.coordinate.setPointOfInterest(null);
        } else if(this.tile != null) {
            this.tile.setPointOfInterest(null);
        }
        this.coordinate = null;
        this.tile = tile;
        tile.setPointOfInterest(this);
        this.earthBound = true;
    }

    public void setCoordinate(Coordinate coordinate) {
        if(this.coordinate != null){
            this.coordinate.setPointOfInterest(null);
        } else if(this.tile != null) {
            this.tile.setPointOfInterest(null);
        }
        this.tile = null;
        this.coordinate = coordinate;
        coordinate.setPointOfInterest(this);
        this.earthBound = false;
    }
}
