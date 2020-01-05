package eu.jgdi.mc.map2mc.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;
import eu.jgdi.mc.map2mc.model.raw.Tuple;
import eu.jgdi.mc.map2mc.model.raw.World;
import eu.jgdi.mc.map2mc.model.raw.WorldImageRaster;
import eu.jgdi.mc.map2mc.model.raw.WorldSection;
import eu.jgdi.mc.map2mc.model.raw.geolocation.Coordinate;
import eu.jgdi.mc.map2mc.model.raw.geolocation.CoordinateSystem;
import eu.jgdi.mc.map2mc.model.raw.geolocation.GeoArea;
import eu.jgdi.mc.map2mc.model.raw.geolocation.GeodeticDatum;
import eu.jgdi.mc.map2mc.renderer.IntermediateOutput;
import eu.jgdi.mc.map2mc.utils.Logger;

public class Parser {

    private static final Logger logger = Logger.logger();

    private WorldRepository worldRepo;

    public Parser(WorldRepository worldRepo) {
        this.worldRepo = worldRepo;
    }

    public void parseWorld() {
        World world = getWorld(worldRepo);

        Map<ChunkLocation, ChunkBuilder> incompleteChunks = new HashMap<>();

        IntermediateOutput ioWriter = new IntermediateOutput(worldRepo.getConfig().getOutputTmpDirectory());
        int count = 0;
        for (WorldSection worldSection : world.getSections()) {
            count++;
            logger.info("Section {0} of {1}...", count, world.getSections().size());
            Tuple<List<ChunkBuilder>> chunkBuilders = WorldMapper.toChunkBuilders(world, worldSection, incompleteChunks);
            List<ChunkBuilder> completeChunks = chunkBuilders.first();
            List<ChunkBuilder> intersectingChunks = chunkBuilders.second();
            logger.debug("- {0} complete and {1} incomplete chunks", completeChunks.size(), incompleteChunks.size());
            List<ChunkBuilder> updatedChunks = incompleteChunks.values().stream()
                    .filter(ChunkBuilder::isComplete)
                    .collect(Collectors.toList());

            updatedChunks.forEach(cs -> {
                completeChunks.add(cs);
                incompleteChunks.remove(cs.getChunkLocation());
            });

            intersectingChunks.forEach(cs ->
                    incompleteChunks.put(cs.getChunkLocation(), cs));

            ioWriter.writeFiles(completeChunks);

            logger.info("  Parsed " + completeChunks.size() + " chunks, " + intersectingChunks.size() + " on hold");
        }
    }

    private static World getWorld(WorldRepository worldRepo) {
        WorldConfig config = worldRepo.getConfig();
        File fileToBeRead = null;
        try {
            fileToBeRead = config.getFileTerrainImageFile();
            BufferedImage terrainImage = ImageIO.read(fileToBeRead);

            fileToBeRead = config.areTerrainAndSurfaceFileTheSame() ? null : config.getFileSurfaceImageFile();
            BufferedImage surfaceImage = fileToBeRead != null ? ImageIO.read(fileToBeRead) : terrainImage;

            int resX = 1, resY = 1;
            int xmin = -0;
            int ymin = -0;
            int xmax = 16;
            int ymax = 16;
            logger.info("- Coordinates ({0},{1}) - ({2},{3})", xmin, ymin, xmax, ymax);
            WorldSection worldSection = new WorldSection(
                    () -> new WorldImageRaster(terrainImage.getRaster(), surfaceImage.getRaster(), worldRepo),
                    resX, resY,
                    new GeoArea(
                            GeodeticDatum.EUREF89,
                            CoordinateSystem.UTM32N,
                            new Coordinate(xmin, ymin),
                            new Coordinate(xmax, ymax)));
            return new World(Collections.singletonList(worldSection));
        } catch (IOException e) {
            logger.error(e, "Failed to read file {0}", fileToBeRead);
            System.exit(1);
            return null;
        }
    }
}
