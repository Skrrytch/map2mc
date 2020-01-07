# map2mc

A program for generating Minecraft maps from real world data.

I used the great `ergor/mcworld` project (https://github.com/ergor/mcworld) as a starting point. 
It was very helpful to get started but uses GeoTIFF format which is quite seldom available. 
I wanted to use another approach but maybe that project is what you were looking for.  

## Introduction

This tool is tested with Minecraft Java-Edition!

The following images of a self drawn map demonstrate the usage of a terrain bitmap, a surface bitmap and the resulting minecraft world.

![alt text][selfdrawn_terrain]
![alt text][selfdrawn_surface]
![alt text][selfdrawn_mcworld]

Notice

- There are two csv files which defines the required mappings
- The text "map2mc" in the surface bitmap results in different block time on the south island
- Four different green colors in the surface bitmap define maddow with flowers, and different kind of trees. And yellow is 'sand'.
- Colors in the surface bitmap which are not mapped to block types result in the block type 'dirt'  

These files are part of the project in `./examples/selfdrawn/`

You can find more details on the wiki page: https://github.com/Skrrytch/map2mc/wiki/Convert-maps-to-Minecraft-worlds

[selfdrawn_surface]: doc/images/selfdrawn-surface.bmp "terrain bitmap"
[selfdrawn_terrain]: doc/images/selfdrawn-terrain.bmp "surface bitmap"
[selfdrawn_mcworld]: doc/images/selfdrawn-mcworld-small.png "terrain bitmap"

__Features:__

- Build a Minecraft map by a *height map image* and a *surface image*
  - The hight map image defines the elevation of the landscape / underwater world
  - The surface image defines the surface block and additional items on it (like saplings)
- Build a Minecraft map by a *'normal' map image* and an optional *mountain image*
  - The map defines the underwater landscape as well as the surface blocks
  - The mountain image is used for the elevation of the landscape (mostly mountains)
- Use can ...
  - map the color index of a height map or 'normal' map to the elevation level of the landscape
  - map the color index of a surface image or 'normal' map to any block typ
  - add additional blocks like `saplings`, `lantern` etc.  on top of the surface using a percentage value (random positioning)
- Configuration of ...
  - output directories (you can render directly to a Minecraft `./saves/region` folder)
  - path to the raster images 
  - path to the CSV files 
  - define the amount of simultanuous threads for rendering
  - the sea level
  - the position (0,0) of the Minecraft world on the map (x/z Offset)

__Limitations:__

- The dimension of the images must be multiples of 512 (e.g. 1024x2048) because each region represents 512 x 512 blocks
- The dimensions of the minecraft world a 1:1 to the dimensions of the raster image: A 512x512 pixel image 
  will produce a 512x512 block world
- Everything in a map will be rendered: Labels (of cities ...), logos, compass rose and more
- You can place sapling but no trees: They have to grow.  

__Known Bugs / unready features:__

- This documentation (README) is not ready. I want to add illustrations and alot more details...

## Usage

Run the program:

```
java -jar map2mc-<VERSION>.jar -dir=<data directory>
```

where `data directory` (in the following referenced by <directory>) is the path to the folder 
containing configuration as well as surface and terrain data. 

When the given directory does not exist then the following files are generated automatically and 
the program stops:

- a config file with default values (`<directory>/config.properties`)
- two empty CSV files with just the headers (`<directory>/terrain.csv` and `<directory>/surface.csv`)
- empty output directories (`<directory>/tmp/` and `<directory>/region/`)

Then you have to create a new or use an existing raster image of your map with an indexed color palette (*.bmp). 
See tips section in the wiki for additional information. 

In most cases you need a mapping file for the terrain (`terrain.csv`)
where each color index must be mapped to a landscape elevation level, relative to the sea level. 

More information about how to build the images and CSV-content can be found in the wiki.

## Compilation

This is a maven-based IntelliJ project. Open it in IntelliJ, or build using maven directly; use `mvn package`, 
or use `mvn compile` to skip generating JAR.

### Credits / resources

[ergor/mcworld](https://github.com/ergor/mcworld)

[Querz/NBT](https://github.com/Querz/NBT)




[Wiki]: https://github.com/Skrrytch/map2mc/wiki/Convert-maps-to-Minecraft-worlds