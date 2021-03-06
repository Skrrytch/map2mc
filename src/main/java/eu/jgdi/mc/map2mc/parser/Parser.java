package eu.jgdi.mc.map2mc.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
        WorldSection worldSection = world.getSection();

        Tuple<List<ChunkBuilder>> chunkBuilders = WorldMapper.toChunkBuilders(
                worldSection,
                incompleteChunks,
                worldRepo);
        List<ChunkBuilder> completeChunks = chunkBuilders.first();
        List<ChunkBuilder> intersectingChunks = chunkBuilders.second();
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

        logger.info("  Parsed " + completeChunks.size() + " complete chunks");
        if (intersectingChunks.size() > 0) {
            logger.warn(
                    "{0} chunks not complete and will not be rendered. Make sure that your images have width and height " +
                            "dimensions which are multiple of 16!", intersectingChunks.size());
        }
    }

    private static World getWorld(WorldRepository worldRepo) {
        WorldConfig config = worldRepo.getConfig();
        File fileToBeRead = null;
        try {
            fileToBeRead = config.getTerrainImageFile();
            BufferedImage terrainImage = ImageIO.read(fileToBeRead);

            fileToBeRead = config.areTerrainAndSurfaceFileTheSame() ? null : config.getSurfaceImageFile();
            BufferedImage surfaceImage = fileToBeRead != null ? ImageIO.read(fileToBeRead) : terrainImage;

            fileToBeRead = config.getMountainsImageFile();
            BufferedImage mountainsImage = fileToBeRead != null ? ImageIO.read(fileToBeRead) : null;

            fileToBeRead = config.getBiomesImageFile();
            BufferedImage biomesImage = fileToBeRead != null ? ImageIO.read(fileToBeRead) : null;

            int xmin = 0;
            int ymin = 0;
            int xmax = 16;
            int ymax = 16;
            logger.info("- Coordinates ({0},{1}) - ({2},{3})", xmin, ymin, xmax, ymax);
            WorldSection worldSection = new WorldSection(
                    () -> new WorldImageRaster(terrainImage, surfaceImage, mountainsImage, biomesImage));
            return new World(worldSection);
        } catch (IOException e) {
            logger.error(e, "Failed to read file {0}", fileToBeRead);
            System.exit(1);
            return null;
        }
    }
}
