# map2mc

A program for generating Minecraft maps from real world data.

I used the great `ergor/mcworld` project (https://github.com/ergor/mcworld) as a starting point. 
It was very helpful to get started but uses GeoTIFF format which is quite seldom available. 
I wanted to use another approach but maybe that project is what you were looking for.  

## Introduction

This tool is tested with Minecraft Java-Edition!

Features:

- Build a Minecraft map from a raster image (the terrain of the landscape)
  - Landscape terrain above and below water by mapping color indexes to landscape levels
  - Speficy the depth of surface blocks (below comes stone, above water if under sea level)
- Using the same or a different raster image for the surface of the laadscape
  - Landscape surface by mapping color indexes to surface block type
  - Adding additional blocks like `saplings`, `lantern` etc. by definable percentage
  - You can usage any block type
- Configuration of ...
  - output directories (you can render directly to a Minecraft `./saves/region` folder)
  - path to the raster images 
  - path to the CSV fles 
  - define the amount of simultanuous threads for rendering
  - the sea level 

## Input data

The starting point for the converter is the configuration file `<directory>/config.properties` 
with the following possibilities:

- A raster __image for terrain__ definition: The elevation of the landscape.
- A __CSV file for terrain__ information mapping the color index to elevation levels.
- An __optional raster image for surface__ definition: The block types of the landscape.
- A __CSV file for surface__ information mapping the color index to block types.
- An output directory for __temporary files__.
- An output directory for __the Minecraft map files__.

These directories can be configured in the configuration file.  

## Usage

Run the program:

```
java -jar map2mc-<VERSION>.jar -dir=<data directory>
```

where `data directory` (in the following referenced by <directory>) is the path to the folder 
containing configuration as well as surface and terrain data. 

When the given directory does not exist then the following files are generated automatically and 
the programm stops:

- a config file with default values (`<directory>/config.properties`)
- two empty CSV files with just the headers (`<directory>/terrain.csv` and `<directory>/surface.csv`)
- empty output directories (`<directory>/tmp/` and `<directory>/region/`)

Then you have to create a raster imager of your map with an indexed color palette (*.bmp). 
See tips section for additional information. In most cases you need a mapping file for the terrain (`terrain.csv`)
where each color index must be mapped to a landscape elevation level, releative to the sea level. 

### Terrain raster image (BMP)

You need a raster image (e.g. in BMP-format) with an indexed color palette where each used color is 
defined by an index. You may use Gimp (https://www.gimp.org) to build such an image with a useful color 
palette (see tips section for details).

The best fitting image for the terrain uses colors to define the landscape elevation (a height map).
But most typical maps use colors to define the surface of a landscape.   

... t.b.c

### Terrain mapping file (CSV)

... t.b.c   

### Surface raster image (BMP)

... t.b.c

### Surface mapping file (CSV)

... t.b.c   

## Using the rendered map in Minecraft

To use the rendered map in Minecraft, copy and paste files from `region` into:

```
<minecraft_game_directory>/saves/<your_save>/region/
```

Or you configure the output directory directly to the Minecraft region folder!

## Tips

... t.b.c

## Compilation

This is a maven-based IntelliJ project. Open it in IntelliJ, or build using maven directly; use `mvn package`, 
or use `mvn compile` to skip generating JAR.

### Credits / resources

[ergor/mcworld](https://github.com/ergor/mcworld)

[Querz/NBT](https://github.com/Querz/NBT)


