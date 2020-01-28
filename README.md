# map2mc

A program for generating Minecraft maps from real world data.

I used the great `ergor/mcworld` project (https://github.com/ergor/mcworld) as a starting point. 
It was very helpful to get started but uses GeoTIFF format which is quite seldom available. 
I wanted to use another approach but maybe that project is what you were looking for.  

## Introduction

This tool is tested with Minecraft Java-Edition!

__Features:__

- Scenario 1: Use a real height map to create the Minecraft landscape
  - Each color in the palette determines a different height, increasing with the index of each color 
  - Optional you can use a mapping file to map the index of colors to heights 
  - Add an additional (surface) map to determine the landscape types (block types).
  - Add an additional (biome) map to determine the landscape biomes
- Scenarion 2: Use a normal landscape map to create the Minecraft landscape
  - Use a mapping file to assign a height to each color.
  - Manipulate the heights with an additional mountain map
  - Add an additional (biome) map to determine the landscape biomes
- About block types:
  - Define the depth of the block types on the surface, e.g. 3 blocks sand (and then stone)
  - Define a stack of block type (e.g. "stone+stone+sand+stone+sand+sand+grass_block")
  - Add additional items on top of the surface, e.g. "grass" or "oak_sapling"
    - Each item is added with a definable probability, e.g. "oak_sapling:5" (meaning 5% oak saplings)
    - You can define multiple items like "oak_sapling:10,grass:50" (10% oak saplings and the rest with 50% grass)    
- About mountains
  - An additional mountain map modifies the height at each block position
  - It is a real height map and uses the color index to define the heights (no mapping file needed) 
- About biomes:
  - An additional biome map will set the corresponding biome on each position.
  - Biomes will influence the way Minecraft renderes gras and trees, spawn animals and monsters and more
  - And: You can (optionally) define the surface mapping depending on the biome: The same color may have different surface in different biomes.
- More options:
  - Restrict the rendered area to a definable rectangle. This is useful for faster tests for a specific area of the map.
  - Calculate the world with several threads in parallel
  - Place the origin of the world (position [0,0]) on any of the possible 512/512 cross points on the map (e.b. at [1024,1536])
  - Save the Minecraft world directly to the 'saves' folder
  - Render different parts (chunks) of the world and combine them 

__Limitations:__

- Getting the best out of existing maps is not easy.
- The height and width and the origin coorindates of the maps (in pixels) must be multiples of 512
- A pixel is mapped 1:1 to a Minecraft block
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