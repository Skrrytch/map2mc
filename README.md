# map2mc

A program for generating Minecraft maps from real world data.

Currently there is support for generating contigous terrain from multiple GeoTIFF heightmaps, 
where location data is defined in the UTM coordinate system.

The goal is to support multiple input data formats and multiple coordinate systems, as well as
additional data layers for things like streets/roads, biomes (forests, beaches, ...) etc.

## Usage

Run program:

```
java -jar map2mc-<VERSION>.jar -directory=<input data directory>
```

where `input data directory` (in the following only <directory>) is the path to the folder containing configuration as well as surface and terrain data. 

map2mc rendering will produce output in the follow locations:

- `<directory>/tmp`: raw, intermediate data is stored here.
- `<directory>/region`: the map itself is stored here.

These directories can be configured in `<directory>/config.properties`.  

To use the rendered map in Minecraft, copy and paste files from `region` into:
```
<minecraft_game_directory>/saves/<your_save>/region/
```

## Compilation

This is a maven-based IntelliJ project. Open it in IntelliJ, or build using maven directly; use `mvn package`, 
or use `mvn compile` to skip generating JAR.

### Credits / resources

[Querz/NBT](https://github.com/Querz/NBT)

[Norwegian Mapping Authority height data](https://www.hoydedata.no)
