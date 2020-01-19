# map2mc

A program for generating Minecraft maps from real world data.

I used the great `ergor/mcworld` project (https://github.com/ergor/mcworld) as a starting point. 
It was very helpful to get started but uses GeoTIFF format which is quite seldom available. 
I wanted to use another approach but maybe that project is what you were looking for.  

## Introduction

This tool is tested with Minecraft Java-Edition!

__Features:__

- Use a real height map to create the Minecraft landscape
  - Each color in the palette determines a different height. Use a mapping file to determine this height yourself.
  - Add a second (surface) map to determine the landscape types (block types).
- Use a typical map to create the Minecraft landscape
  - Use a mapping file to assign a height to each color.
  - Add a mountain map to manipulate the heights on land
- About landscape heights:
  - Define the depth in the water and the heights on land
  - Define the height of the general water surface
- About block types:
  - Define the depth of the block types on the surface, e.g. 3 sand (and then stone)
  - Add items like grass, oak_sapling, orange_tulip, lantern ...
  - Specify probabilities with which the items are added
- Supports a biome image and CSV mapping to defines biomes
  - Will set the corresponding biome on each position.
  - Biomes will influence the way Minecraft renderes gras, tree and much more
- Calculate the world with several threads in parallel
- Save the Minecraft world directly to the 'saves' folder

__Limitations:__

- Getting the best out of existing maps is not easy.
- The height and width of the maps (in pixels) must be multiples of 512
- A pixel is mapped 1:1 to a Minecraft block
- The zero point of the Minecraft world is always the top left corner of the map
- All texts are also rendered in the world
- You can put 'saplings' but no trees. These have to grow first! (but there is a trick)


__Example "SelfDrawn"__

The following images of a self drawn map demonstrate the usage of 
a height map and a surface map with the resulting minecraft world.

![Height Map][selfdrawn_terrain]
![Surface Map][selfdrawn_surface]
![Minecraft][selfdrawn_mcworld]

[selfdrawn_terrain]: doc/images/selfdrawn-terrain-small.bmp "height map"
[selfdrawn_surface]: doc/images/selfdrawn-surface-small.bmp "surface map"
[selfdrawn_mcworld]: doc/images/selfdrawn-mcworld-small.png "Minecraft"

__Example "Map"__

The following images of a real drawn map (Aventuria - the dark eye) demonstrate the usage of 
a normal map and a mountain map with the resulting minecraft world.

![Normal Map][realmap_terrain]
![Mountains Map][realmap_mountains]
![Minecraft][realmap_mcworld]

[realmap_terrain]: doc/images/realmap-terrain-small.bmp "normal map"
[realmap_mountains]: doc/images/realmap-mountains-small.bmp "mountains map"
[realmap_mcworld]: doc/images/realmap-mcworld-small.png "Minecraft"

The area which you can see in the screenshot from Minecraft 
is highlighted in the terrain map.

## Running the application

map2mc only needs one directory for execution, in which all data including a configuration file are then available:

```
java -jar map2mc-<VERSION>.jar -dir=<data directory>
```

If the directory does __not exist__, map2mc will create the directory, create an initial configuration file and empty mapping files. This is a good start for your own map.

If the directory __exists__, map2mc expects at least one configuration file `config.properties` as well as the necessary image and mapping files.

The contents of the directory and the configuration are explained in detail on the wiki pages. 
There you will also find further practical information about map creation and manipulation.

## Compilation

This is a maven-based IntelliJ project. Open it in IntelliJ, or build using maven directly; use `mvn package`, 
or use `mvn compile` to skip generating JAR.

### Credits / resources

[ergor/mcworld](https://github.com/ergor/mcworld)

[Querz/NBT](https://github.com/Querz/NBT)

[Wiki]: https://github.com/Skrrytch/map2mc/wiki/