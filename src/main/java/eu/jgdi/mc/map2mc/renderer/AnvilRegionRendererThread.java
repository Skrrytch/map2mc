package eu.jgdi.mc.map2mc.renderer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.mca.Chunk;
import net.querz.nbt.mca.MCAFile;
import net.querz.nbt.mca.MCAUtil;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.config.csv.BiomesCsvContent;
import eu.jgdi.mc.map2mc.config.csv.BlockStack;
import eu.jgdi.mc.map2mc.config.csv.CompoundDef;
import eu.jgdi.mc.map2mc.config.csv.SurfaceCsvContent;
import eu.jgdi.mc.map2mc.config.csv.TerrainCsvContent;
import eu.jgdi.mc.map2mc.model.minecraft.Block;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.raw.ChunkInfoMap;
import eu.jgdi.mc.map2mc.model.raw.RegionInfoMap;
import eu.jgdi.mc.map2mc.utils.Logger;

public class AnvilRegionRendererThread extends Thread {

    private static final Logger logger = Logger.logger();

    private final long fileNumber;

    private final Set<String> messages = new HashSet<>();

    private final CountDownLatch latch;

    private final CompoundDef defaultCompound;

    private final CompoundDef waterCompound;

    private final CompoundDef stoneCompound;

    private final CompoundDef bedrockCompound;

    private final String fileName;

    private final int baseLevel;

    private final int seaLevel;

    private RegionInfoMap regionInfoMap;

    private WorldConfig config;

    private WorldRepository worldRepo;

    private TerrainCsvContent terrainCsvContent;

    private SurfaceCsvContent surfaceCsvContent;

    private BiomesCsvContent biomeCsvContent;

    public AnvilRegionRendererThread(
            long fileNumber,
            String fileName,
            CountDownLatch latch,
            RegionInfoMap regionInfoMap,
            WorldRepository worldRepo) {
        this.latch = latch;
        this.fileNumber = fileNumber;
        this.fileName = fileName;
        this.regionInfoMap = regionInfoMap;
        this.worldRepo = worldRepo;
        this.config = worldRepo.getConfig();
        this.terrainCsvContent = config.getTerrainCsvContent();
        this.surfaceCsvContent = config.getSurfaceCsvContent();
        this.biomeCsvContent = config.getBiomesCsvContent();
        this.waterCompound = worldRepo.getBlockCompoundDef(Block.WATER.getBlockId());
        this.stoneCompound = worldRepo.getBlockCompoundDef(Block.STONE.getBlockId());
        this.bedrockCompound = worldRepo.getBlockCompoundDef(Block.BEDROCK.getBlockId());
        this.defaultCompound = worldRepo.getBlockCompoundDef(Block.DIRT.getBlockId());
        this.baseLevel = config.getBaseLevel();
        this.seaLevel = config.getSeaLevel();
    }

    @Override
    public void run() {
        try {
            renderRegion();
        } catch (IOException | RuntimeException e) {
            logger.error(e, "Failed to render in thread #{0}: {1}", fileName, e.getLocalizedMessage());
        } finally {
            latch.countDown();
        }
    }

    private void renderRegion() throws IOException {

        int regionX = regionInfoMap.getLocation().getX() - (worldRepo.getConfig().getOriginX() / 512);
        int regionZ = regionInfoMap.getLocation().getZ() - (worldRepo.getConfig().getOriginY() / 512);
        MCAFile region = new MCAFile(regionX, regionZ);

        long chunkCount = 0;
        for (int z = 0; z < Constants.REGION_LEN_Z; z++) {
            for (int x = 0; x < Constants.REGION_LEN_X; x++) {
                chunkCount++;
                ChunkLocation location = new ChunkLocation(x, z, ReferenceFrame.REGION);
                ChunkInfoMap chunkInfoMap = regionInfoMap.getChunk(location);

                Chunk chunk = renderChunk(chunkInfoMap, x, z, regionX, regionZ);

                region.setChunk(x, z, chunk);
            }
        }

        String nameFromRegionLocation = MCAUtil.createNameFromRegionLocation(regionX, regionZ);
        Path filePath = Paths.get(config.getOutputRegionDirectory().getPath(), nameFromRegionLocation);
        MCAUtil.writeMCAFile(region, filePath.toFile(), true);

        logger.info(
                "  Region file {0} (''{1}'') processed: {2} chunks written to file {3}",
                fileNumber,
                fileName,
                chunkCount,
                filePath.getFileName());
    }

    private Chunk renderChunk(ChunkInfoMap chunkInfoMap, int chunkX, int chunkZ, int regionX, int regionZ) {

        int relRegionX = regionX * 512;
        int relRegionZ = regionZ * 512;
        int relChunkX = chunkX * 16;
        int relChunkZ = chunkZ * 16;

        Chunk chunk = Chunk.newChunk();
        for (int z = 0; z < Constants.CHUNK_LEN_Z; z++) {
            for (int x = 0; x < Constants.CHUNK_LEN_X; x++) {
                int level;
                int worldX = relRegionX + relChunkX + x;
                int worldZ = relRegionZ + relChunkZ + z;
                boolean undefined = (chunkInfoMap.getFlagField(x, z) == 0);
                if (undefined) {
                    level = buildBlocksForUndefined(z, x, chunk);
                } else {
                    level = buildBlocks(z, x, chunkInfoMap, chunk, worldX, worldZ);
                }
                if (worldRepo.isNorthWestEdge(worldX, worldZ)) {
                    worldRepo.setWorldRectNorthWestY(level);
                } else if (worldRepo.isCenter(worldX, worldZ)) {
                    worldRepo.setWorldRectCenterY(level);
                } else if (worldRepo.isSouthEastEdge(worldX, worldZ)) {
                    worldRepo.setWorldRectSouthEastY(level);
                }
            }
        }

        chunk.cleanupPalettesAndBlockStates();
        return chunk;
    }

    private int buildBlocks(int z, int x, ChunkInfoMap chunkInfoMap, Chunk chunk, int worldX, int worldZ) {
        int level;
        int currentLevel = 0;
        byte terrainIndex = chunkInfoMap.getTerrainIndex(x, z);
        byte surfaceIndex = chunkInfoMap.getSurfaceIndex(x, z);
        byte mountainIndex = chunkInfoMap.getMountainLevel(x, z);
        byte biomeIndex = chunkInfoMap.getBiomeIndex(x, z);

        BiomesCsvContent.Record biomeRecord = biomeCsvContent.getByColorIndex(biomeIndex);
        TerrainCsvContent.Record terrainRecord = terrainCsvContent.getByColorIndex(terrainIndex);
        SurfaceCsvContent.Record surfaceRecord = surfaceCsvContent.getByColorIndex(surfaceIndex);

        if (surfaceRecord == null) {
            logger.error("Unmapped  block at world position ({0},{1}),color index {2}", worldX, worldZ, surfaceIndex);
        }
        // get compounds from stack definition
        List<CompoundDef> surfaceCompoundList = getSurfaceCompoundDefList(surfaceRecord);
        List<CompoundDef> itemCompoundList = getItemCompoundDefList(surfaceRecord);

        // Error logging
        validateStack(surfaceCompoundList, worldX, worldZ, surfaceIndex);
        validateStack(itemCompoundList, worldX, worldZ, surfaceIndex);

        int mountainLevelStart = mountainIndex + config.getMountainsLevelStart();
        int terrainLevelOffset = terrainRecord != null ? terrainRecord.getLevel() : terrainIndex - seaLevel;
        int topLevel = seaLevel + terrainLevelOffset + mountainLevelStart;
        if (biomeRecord != null) {
            chunk.setBiomeAt(x, z, biomeRecord.getBiomeId()); // jungle
        }
        if (terrainLevelOffset < 0) { // under water
            int waterDepth = Math.abs(terrainLevelOffset);
            int stoneCount = baseLevel + Math.max(0, topLevel - surfaceCompoundList.size());
            currentLevel = buildBaseStack(currentLevel, chunk, x, z, stoneCount, topLevel);
            currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceCompoundList);
            if (itemCompoundList != null) {
                currentLevel = buildItemStack(currentLevel, chunk, x, z, itemCompoundList);
                waterDepth -= itemCompoundList.size();
            }
            currentLevel = buildWaterStack(currentLevel, chunk, x, z, waterDepth);
        } else { // above water
            int stoneCount = Math.max(0, topLevel - surfaceCompoundList.size());

            currentLevel = buildBaseStack(currentLevel, chunk, x, z, stoneCount + baseLevel, topLevel);
            currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceCompoundList);

            // additional block, e.g. a sapling

            if (itemCompoundList != null) {
                currentLevel = buildItemStack(currentLevel, chunk, x, z, itemCompoundList);
            }
        }
        level = currentLevel;
        return level;
    }

    private List<CompoundDef> getSurfaceCompoundDefList(SurfaceCsvContent.Record surfaceRecord) {
        List<CompoundDef> surfaceCompoundList;
        if (surfaceRecord != null) {
            surfaceCompoundList = surfaceRecord.getSurfaceStack().getCompoundDefList();
        } else {
            surfaceCompoundList = List.of(defaultCompound);
        }
        return surfaceCompoundList;
    }

    private List<CompoundDef> getItemCompoundDefList(SurfaceCsvContent.Record surfaceRecord) {
        if (surfaceRecord == null || !surfaceRecord.hasItems()) {
            return null;
        }
        List<BlockStack> possibleItemStacks = surfaceRecord.getItemStackList();
        return possibleItemStacks.stream()
                .filter(stack -> Math.random() <= stack.getProbability()) // choose one of the stacks
                .map(BlockStack::getCompoundDefList) // map to compound def list
                .findAny() // probability match: Use this stack
                .orElse(null); // no match: No item at all
    }

    private void validateStack(List<CompoundDef> compoundDefList, int worldX, int worldZ, byte surfaceIndex) {
        if (compoundDefList == null) {
            return;
        }
        compoundDefList.stream()
                .filter(CompoundDef::isUndefined)
                .distinct()
                .forEach(def -> logger.error(
                        "UNDEFINED block at world position ({0},{1}), color index {2}",
                        worldX,
                        worldZ,
                        surfaceIndex));
    }

    private int buildBlocksForUndefined(int z, int x, Chunk chunk) {
        int currentLevel = 0;
        // We have no information about the blocks but the region is not finished
        // In this case we build standard stone blocks of baselevel height and water blocks of seaLevel height
        currentLevel = buildBaseStack(currentLevel, chunk, x, z, baseLevel, baseLevel);
        currentLevel = buildWaterStack(currentLevel, chunk, x, z, seaLevel);
        return currentLevel;
    }

    private int buildItemStack(
            int startLevel,
            Chunk chunk,
            int x,
            int z,
            List<CompoundDef> itemStack) {
        if (itemStack == null) {
            return startLevel;
        }
        int currentLevel = startLevel;
        for (CompoundDef compountDef : itemStack) {
            if (!compountDef.isAir()) {
                chunk.setBlockStateAt(x, currentLevel, z, compountDef.getCompoundTag(), false);
            }
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildSurfaceStack(
            int startLevel, Chunk chunk, int x, int z,
            List<CompoundDef> surfaceBlocks) {
        if (surfaceBlocks == null || surfaceBlocks.isEmpty()) {
            return startLevel;
        }
        int currentLevel = startLevel;
        for (CompoundDef surfaceCompound : surfaceBlocks) {
            if (!surfaceCompound.isAir()) {
                chunk.setBlockStateAt(x, currentLevel, z, surfaceCompound.getCompoundTag(), false);
            }
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildWaterStack(int startLevel, Chunk chunk, int x, int z, int waterDepthCount) {
        if (waterDepthCount <= 0) {
            return startLevel;
        }
        int currentLevel = startLevel;
        for (int i = 0; i < waterDepthCount; i++) {
            chunk.setBlockStateAt(x, currentLevel, z, waterCompound.getCompoundTag(), false);
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildBaseStack(int startLevel, Chunk chunk, int x, int z, int baseCount, int topLevelWithoutWater) {
        int currentLevel = startLevel;
        for (int i = 0; i < baseCount; i++) {
            if (baseCount > 3 && i < 3) {
                chunk.setBlockStateAt(x, currentLevel, z, bedrockCompound.getCompoundTag(), false);
            } else {
                CompoundTag compound = stoneCompound.getCompoundTag();
                chunk.setBlockStateAt(x, currentLevel, z, compound, false);
            }
            currentLevel++;
        }
        enrichNaturalResources(chunk, x, z, baseLevel + topLevelWithoutWater);
        return currentLevel;
    }

    /**
     * example 1: 3% => (blockCount * 0.03) + (random-0.5)*(blockCount * 0.03)
     * - 3% for 10 blocks = 0.3 + [-0.5...+0.5]*0.3 = 0.15 ... 0.45 (always 0)
     * - 3% for 100 blocks = 3 +  [-0.5...+0.5]*(3/5) = 1.5 ... 4.5 (2 - 5)
     * - 50% for 100 blocks = 50 + [-0.5...+0.5]*(50/5) = 40...60
     */
    private void enrichNaturalResources(Chunk chunk, int x, int z, int topLevelWithoutWater) {
        for (WorldConfig.NaturalResource naturalResource : config.getNaturalResources()) {
            if (naturalResource.getRequiredTopLevel() > topLevelWithoutWater) {
                continue;
            }
            long maxStackCount = naturalResource.calculateCurrentMaxStack(3, topLevelWithoutWater);
            double expectedAverage = maxStackCount * naturalResource.getPercentFactor();
            long calculatedStackCount;
            if (expectedAverage < 1) {
                calculatedStackCount = Math.random() < expectedAverage ? 1 : 0;
            } else {
                double randomDiff = Math.random() - 0.5d;
                calculatedStackCount = (int) (expectedAverage + randomDiff * expectedAverage / 2d);
                calculatedStackCount = Math.min(calculatedStackCount, maxStackCount);
            }
            if (calculatedStackCount > 0) {
                int startY = naturalResource.getMinLevel()
                        + (int) Math.floor(Math.random() * (maxStackCount - calculatedStackCount));
                CompoundDef blockCompoundDef = worldRepo.getBlockCompoundDef(naturalResource.getBlock().getBlockId());
                for (int y = startY; y < startY + calculatedStackCount; y++) {
                    chunk.setBlockStateAt(x, y, z, blockCompoundDef.getCompoundTag(), false);
                }
            }
        }
    }

    public Set<String> getMessages() {
        return messages;
    }
}
