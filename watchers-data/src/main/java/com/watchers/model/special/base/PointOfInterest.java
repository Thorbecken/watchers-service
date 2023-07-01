package com.watchers.model.special.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import com.watchers.model.coordinate.Coordinate;
import com.watchers.model.environment.Tile;
import com.watchers.model.special.crystal.AquiferCrystal;
import com.watchers.model.special.crystal.HotSpotCrystal;
import com.watchers.model.special.crystal.TectonicCrystal;
import com.watchers.model.special.crystal.WindCrystal;
import com.watchers.model.special.life.GreatFlora;
import lombok.Data;

import jakarta.persistence.*;

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
        @JsonSubTypes.Type(value = WindCrystal.class, name = "WindCrystal"),
        @JsonSubTypes.Type(value = GreatFlora.class, name = "GreatFlora")
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
        // pointOfInterest is not deleted from the tile
        // because this would remove it from the database through orphan removal
        this.coordinate = null;
        this.tile = tile;
        if (tile != null) {
            tile.setPointOfInterest(this);
        }
        this.earthBound = true;
    }

    public void setCoordinate(Coordinate coordinate) {
        // pointOfInterest is not deleted from the tile
        // because this would remove it from the database through orphan removal
        this.tile = null;
        this.coordinate = coordinate;
        if (coordinate != null) {
            coordinate.setPointOfInterest(this);
        }
        this.earthBound = false;
    }

    public abstract PointOfInterest createClone(Coordinate coordinate, Tile tile);
}
