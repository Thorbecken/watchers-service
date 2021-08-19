package com.watchers.model.climate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.watchers.model.common.Views;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Table(name = "outgoingAircurrents")
@SequenceGenerator(name="OAC_Gen", sequenceName="OAC_Seq", allocationSize = 1)
public class OutgoingAircurrent {

        @Id
        @JsonView(Views.Internal.class)
        @GeneratedValue(generator="OAC_Gen", strategy = GenerationType.SEQUENCE)
        @Column(name = "outgoingAircurrent_id", nullable = false)
        private Long id;

        @JsonIgnore
        @OneToOne
        private SkyTile startingSky;

        @OneToMany(mappedBy = "outgoingAircurrent", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
        private List<Aircurrent> outgoingAircurrent = new ArrayList<>(2);

        public OutgoingAircurrent(SkyTile startingSky){
                this.startingSky = startingSky;
        }

        public void add(Aircurrent aircurrent){
                this.outgoingAircurrent.add(aircurrent);
        }

        public void clear(){
                this.getOutgoingAircurrent().clear();
        }
}
