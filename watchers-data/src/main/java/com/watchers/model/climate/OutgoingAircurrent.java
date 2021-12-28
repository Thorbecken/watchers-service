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
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sky_id", nullable = false)
        private SkyTile startingSky;

        @JsonView(Views.Public.class)
        @OneToMany(mappedBy = "outgoingAircurrent", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
        private List<Aircurrent> aircurrentList = new ArrayList<>(2);

        public OutgoingAircurrent(SkyTile startingSky){
                this.startingSky = startingSky;
        }

        public void add(Aircurrent aircurrent){
                this.aircurrentList.add(aircurrent);
        }

        public void clear(){
                this.getAircurrentList().clear();
        }

        @Override
        public String toString() {
                return "OutgoingAircurrent{" +
                        "id=" + id +
                        ", aircurrentList=" + aircurrentList +
                        '}';
        }
}
