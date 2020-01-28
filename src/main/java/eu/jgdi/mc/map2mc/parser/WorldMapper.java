package eu.jgdi.mc.map2mc.parser;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.BlockLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.MinecraftLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.raw.Tuple;
import eu.jgdi.mc.map2mc.model.raw.WorldRaster;
import eu.jgdi.mc.map2mc.model.raw.WorldSection;
import eu.jgdi.mc.map2mc.utils.Logger;

public class WorldMapper {

    private static Logger logger = Logger.logger();

    /**
     * @param worldSection (in param): the worldsection
     * @param incompleteChunks (in/out param): chunks that are _not_ entirely inside this world section
     * and thus are incomplete
     * @param worldRepo
     *
     * @return whether there are intersecting surface chunks
     */
    public static Tuple<List<ChunkBuilder>> toChunkBuilders(
            WorldSection worldSection,
            Map<ChunkLocation, ChunkBuilder> incompleteChunks,
            WorldRepository worldRepo) {

        Rectangle rectangle = worldRepo.getConfig().getRectangle();

        WorldRaster raster = worldSection.getRaster();

        Map<ChunkLocation, ChunkBuilder> chunkBuilderMap = new HashMap<>();

        int startX = rectangle != null ? (int) (rectangle.getX()) : 0;
        int startY = rectangle != null ? (int) (rectangle.getY()) : 0;
        int maxX = rectangle != null ? (int) (rectangle.getX() + rectangle.getWidth()) : raster.getWidth();
        int maxY = rectangle != null ? (int) (rectangle.getY() + rectangle.getHeight()) : raster.getHeight();

        worldRepo.setExtends(startX, startY, maxX, maxY);

        int iterationCount = (maxX - startX) * (maxY - startY);
        logger.info(
                "Area of map:   ({0,number,#},{1,number,#}) to ({2,number,#},{3,number,#}) with dimensions {4,number,#}x{5,number,#}",
                startX,
                startY,
                maxX,
                maxY,
                maxX - startX,
                maxY - startY);
        logger.info(
                "Area of world: ({0,number,#},{1,number,#}) to ({2,number,#},{3,number,#}) with dimensions {4,number,#}x{5,number,#}",
                worldRepo.getWorldRectNorthWestX(),
                worldRepo.getWorldRectNorthWestZ(),
                worldRepo.getWorldRectSouthEastX(),
                worldRepo.getWorldRectSouthEastZ(),
                worldRepo.getWorldRectWidth(),
                worldRepo.getWorldRectHeight());

        logger.debug(
                "Iterate over {0}x{1} pixel area ... ({2} steps)", (maxX - startX), (maxY - startY), iterationCount);

        int count = 0;
        for (int pixelY = startY; pixelY < maxY; pixelY++) {

            for (int pixelX = startX; pixelX < maxX; pixelX++) {
                count++;
                if (count % 1000000 == 0) {
                    logger.debug("Iteration {0} of {1}", count, iterationCount);
                }

                // Now find what pixel that would be if we treat the whole world as one image:
                double worldPixelX = pixelX;
                double worldPixelY = pixelY;

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
