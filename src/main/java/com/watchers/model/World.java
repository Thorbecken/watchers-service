package com.watchers.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "world")
public class World {

    @Id
    @SequenceGenerator(name="World_Gen", sequenceName="World_Seq", allocationSize = 1)
    @GeneratedValue(generator="World_Gen", strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @OneToMany(mappedBy = "world")
    private Set<Tile> tiles;

    public Set<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(Set<Tile> tiles) {
        this.tiles = tiles;
    }
}
