package eu.jgdi.mc.map2mc.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.jgdi.mc.map2mc.model.minecraft.coordinates.BlockLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.MinecraftLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.raw.Tuple;
import eu.jgdi.mc.map2mc.model.raw.World;
import eu.jgdi.mc.map2mc.model.raw.WorldRaster;
import eu.jgdi.mc.map2mc.model.raw.WorldSection;
import eu.jgdi.mc.map2mc.model.raw.geolocation.Coordinate;
import eu.jgdi.mc.map2mc.utils.Logger;

public class WorldMapper {

    private static Logger logger = Logger.logger();

    /**
     * @param worldSection (in param): the worldsection
     * @param incompleteChunks (in/out param): chunks that are _not_ entirely inside this world section
     * and thus are incomplete
     *
     * @return whether there are intersecting surface chunks
     */
    public static Tuple<List<ChunkBuilder>> toChunkBuilders(
            World world,
            WorldSection worldSection,
            Map<ChunkLocation, ChunkBuilder> incompleteChunks) {

        Coordinate worldOrigin = world.getArea().getOrigin();
        Coordinate sectionOrigin = worldSection.getArea().getOrigin();

        WorldRaster raster = worldSection.getRaster();

        Map<ChunkLocation, ChunkBuilder> chunkBuilderMap = new HashMap<>();

        int iterationCount = raster.getWidth() * raster.getHeight();
        logger.debug(
                "Iterate over {0}x{1} pixel area ... ({2} steps)", raster.getWidth(), raster.getHeight(), iterationCount);
        int count = 0;
        for (int pixelY = 0; pixelY < raster.getHeight(); pixelY++) {

            for (int pixelX = 0; pixelX < raster.getWidth(); pixelX++) {
                count++;
                if (count % 1000000 == 0) {
                    logger.debug("Iteration {0} of {1}", count, iterationCount);
                }

                // Now find what pixel that would be if we treat the whole world as one image:
                double worldPixelX = pixelX + sectionOrigin.x - worldOrigin.x;
                double worldPixelY = pixelY + sectionOrigin.y - worldOrigin.y;

                Tuple<MinecraftLocation> locationTuple = new BlockLocation(
                        (int) (worldPixelX),
                        (int) (worldPixelY))
                        .tryReferencedTo(ReferenceFrame.CHUNK);

                BlockLocation blockLocation = (BlockLocation) locationTuple.first();
                ChunkLocation chunkLocation = (ChunkLocation) locationTuple.second();

                WorldRaster.Info info = raster.getPixelInfo(pixelX, pixelY);

                ChunkBuilder chunkBuilder;
                if (incompleteChunks.containsKey(chunkLocation)) {
                    chunkBuilder = incompleteChunks.get(chunkLocation);
                } else {
                    chunkBuilder = chunkBuilderMap.computeIfAbsent(chunkLocation, ChunkBuilder::new);
                }

                chunkBuilder.insert(blockLocation, info);
            }
        }

        return new Tuple<>(
                chunkBuilderMap.values().stream()
                        .filter(ChunkBuilder::isComplete)
                        .collect(Collectors.toList()),
                chunkBuilderMap.values().stream()
                        .filter(ChunkBuilder::isIncomplete)
                        .collect(Collectors.toList())
        );
    }
}
