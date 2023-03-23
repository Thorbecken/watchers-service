# watchers-service
Deze service genereerd een willekeurig wereld waarop dieren gesimuleerd worden.
Adviced VM options: -Dspring.profiles.active=dev -Xms256m -Xmx2048m
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