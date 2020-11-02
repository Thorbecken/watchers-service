import com.watchers.model.coordinate.CoordinateFactory;
import com.watchers.model.environment.Continent;
import com.watchers.model.environment.SurfaceType;
import com.watchers.model.environment.Tile;
import com.watchers.model.world.World;
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
        Continent continent = new Continent(world, SurfaceType.OCEANIC);

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
        Tile tile = world.getTile(x,y);
        List<Tile> neighbours = tile.getNeighbours();

        assertEquals(4, neighbours.size());

    }
}