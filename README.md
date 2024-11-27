# watchers-service
This service generates a random world on which plants and animals are simulated.
There are endpoints that allow interaction in the world by adding plant and wildlife or adding and removing world modifying crystals.
Advised VM options: -Dspring.profiles.active=dev -Xms256m -Xmx2048m
usage of around 4GB memory

# h2 database checks
select count(1) from "animal";
select count(1) from "river";
select count(1) from "watershed";
select count(1) from "continent";
select count(1) from "coordinate";
select count(1) from "tile";
select count(1) from "biome";
select count(1) from "climate";
select count(1) from "sky";

# todo
 - make extra an extra air current towards the hottest neighbour or towards itself if no hotter neighbour exists of about half total current strength.
 - make rain based on temperature differences
 - make water evaporate on land
 - make erosion more harsh

WATCH-014: Make a 12-year cycle in which each year represents a month. Calculate the max height in which the equator moves.
    Take this as the quarter height and move the other half year to the lowest height in which the equator moves.
    Adjust the mean temperature on base of the normal temperature per coordinate altitude.

Watch-015: When a continental turns happens start the following: 
    Make a 10-year simulation of the heath transfer. Make another 10-year simulation of the water cycle without plants.
    And lastly make a 10-year simulation with plants and animals.

Watch-016: Calculate the day heath difference on a tile on basis on how many tiles it would take to reach the ocean or a lake.
    Use this number and multiply it with the magic number X to calculate the temperature swing.
    Detract the mean temperature with the magic number to calculate the lower night temperature and add it to create the higher day temperature.
    Use the lower temperature to calculate the rainfall and the higher number to calculate the moisture transfer to the next tile.

Watch-017: Change the water cycle to the following:
    - First calculate the rainfall on the lowest temperature.
    - Second let the plants drink and evaporate their usage.
    - Third use the max temperature to transfer an amount of moisture.
    - fourthly use the over excess amount of water to create rivers.

Watch-018: Make two types of players:
    - Demiurge (Solo) that can have one of grass, plant and animal life.
    - Council (Team) that is a team of multiple players that can have one grass, plant or animal life.
    - Team score is calculated on basis of each grass, plant and animal life.

Watch-019: Rework ContinentalSplitter class
    - The doSomething method has unreachable code if the out commented code is added.
    - The out commented code causes an exception as explained in the comments
    - Add a methode that looks if the continent can be split between a surface continent and an ocean continent.
        - Let the method split the continent in two continents; a continent with all the above sea level coordinates and one with all the below sea level coordinates.
        - Let the method split the coordinates of these new continents based if there connected.
        - Let the method add coordinates of continents that are surrounded by another continent to the surrounding continent.
        - If only one continent survives split the continent with the default method.

Watch-020: Rework the API's to allow for more nuanced interactions with the world and make is possible for the frontend to use these.

Watch-021: Rework the Biome and Flora class. Make it more realistic.
    - Make it that sunlight is used to calculate maximum growth, which in turn needs water.
    - Sunlight can be inferred from the latitude of the climate. The climate could hold the amount of sunlight.
    - Trees have priority on sunlight uptake and grass has priority on water uptake. So if any sunlight is left over for the grass, the grass gets the first chance to grow, and use water.