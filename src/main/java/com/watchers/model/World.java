package com.watchers.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "world")
public class World {

    @Id
    @GeneratedValue
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
