package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "incommingAircurrents")
@SequenceGenerator(name = "IAC_Gen", sequenceName = "IAC_Seq", allocationSize = 1)
public class IncommingAircurrent {

    @Id
    @JsonView(Views.Internal.class)
    @GeneratedValue(generator = "IAC_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "incommingAircurrent_id", nullable = false)
    private Long id;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sky_id", nullable = false)
    private SkyTile endingSky;

    @JsonView(Views.Public.class)
    @OneToMany(mappedBy = "incommingAircurrent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Aircurrent> aircurrentList = new ArrayList<>(2);

    public IncommingAircurrent(SkyTile endingSky) {
        this.endingSky = endingSky;
    }

    public void add(Aircurrent aircurrent) {
        this.aircurrentList.add(aircurrent);
        aircurrent.setIncommingAircurrent(this);
        aircurrent.setEndingSky(this.endingSky);
    }

    public void addAll(List<Aircurrent> aircurrents) {
        this.aircurrentList.addAll(aircurrents);
        aircurrents.forEach(aircurrent -> {
            aircurrent.setIncommingAircurrent(this);
            aircurrent.setEndingSky(this.endingSky);
        });
    }

    public void clear() {
        this.getAircurrentList().clear();
    }

    @Override
    public String toString() {
        return "IncommingAircurrent{" +
                "id=" + id +
                ", aircurrentList=" + aircurrentList +
                '}';
    }

    public IncommingAircurrent createClone() {
        IncommingAircurrent clone = new IncommingAircurrent();
        clone.setId(this.getId());
        clone.setEndingSky(endingSky.createBareboneClone());
        return clone;
    }
}