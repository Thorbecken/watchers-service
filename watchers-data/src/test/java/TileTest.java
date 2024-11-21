import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.enums.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.Continent;
import com.watchers.model.world.World;
import com.watchers.model.world.WorldSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    private World world;

    @BeforeEach
    void setup(){
        this.world = new World(3, 3);
        this.world.setWorldSettings(new WorldSettings());
        this.world.getWorldSettings().setLifePreSeeded(true);

        Continent continent = new Continent(world, SurfaceType.OCEAN);

        for (int x = 1; x <= 3; x++) {
            for (int y = 1; y <= 3; y++) {
                world.getCoordinates().add(CoordinateFactory.createCoordinate(x,y,world, continent));
            }
        }
    }

    @ParameterizedTest
    @SuppressWarnings("all")
    @CsvSource({"1,1", "1,2", "1,3", "2,1", "2,2", "2,3", "3,1", "3,3", "3,3"})
    void getNeighbours(long x, long y) {
        Tile tile = world.getCoordinate(x,y).getTile();
        List<Tile> neighbours = tile.getNeighbours();

        assertEquals(4, neighbours.size());

    }
}