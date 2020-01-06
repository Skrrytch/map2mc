package eu.jgdi.mc.map2mc.renderer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.mca.Chunk;
import net.querz.nbt.mca.MCAFile;
import net.querz.nbt.mca.MCAUtil;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
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

    private final CountDownLatch latch;

    private final CompoundTag waterCompound;

    private final CompoundTag stoneCompound;

    private final CompoundTag unknownCompound;

    private final CompoundTag bedrockCompound;

    private long fileNumber;

    private RegionInfoMap regionInfoMap;

    private WorldConfig config;

    private WorldRepository worldRepo;

    private TerrainCsvContent terrainCsvContent;

    private SurfaceCsvContent surfaceCsvContent;

    public AnvilRegionRendererThread(
            long fileCount,
            CountDownLatch latch,
            RegionInfoMap regionInfoMap,
            WorldRepository worldRepo) {
        this.latch = latch;
        this.fileNumber = fileCount;
        this.regionInfoMap = regionInfoMap;
        this.worldRepo = worldRepo;
        this.config = worldRepo.getConfig();
        this.terrainCsvContent = config.getTerrainCsvContent();
        this.surfaceCsvContent = config.getSurfaceCsvContent();
        this.waterCompound = worldRepo.getBlockCompoundId(Block.WATER.getBlockId());
        this.stoneCompound = worldRepo.getBlockCompoundId(Block.STONE.getBlockId());
        this.unknownCompound = worldRepo.getBlockCompoundId(Block.UNKNOWN.getBlockId());
        this.bedrockCompound = worldRepo.getBlockCompoundId(Block.BEDROCK.getBlockId());
    }

    @Override
    public void run() {
        try {
            renderRegion();
        } catch (IOException e) {
            logger.error(e, "Failed to render in thread #{0}: {1}", fileNumber, e.getLocalizedMessage());
        } finally {
            latch.countDown();
        }
    }

    private void renderRegion() throws IOException {

        int regionX = regionInfoMap.getLocation().getX();
        int regionZ = regionInfoMap.getLocation().getZ();
        MCAFile region = new MCAFile(regionX, regionZ);

        long chunkSum = Constants.REGION_LEN_Z * Constants.REGION_LEN_X;
        long chunkCount = 0;
        for (int z = 0; z < Constants.REGION_LEN_Z; z++) {
            for (int x = 0; x < Constants.REGION_LEN_X; x++) {
                chunkCount++;
                ChunkLocation location = new ChunkLocation(x, z, ReferenceFrame.REGION);
                ChunkInfoMap chunkInfoMap = regionInfoMap.getChunk(location);

                Chunk chunk = renderChunk(chunkInfoMap);

                region.setChunk(x, z, chunk);
                if (chunkCount % 256 == 0) {
                    logger.info("  File {0}: Chunk {1} of {2} done", fileNumber, chunkCount, chunkSum);
                }
            }
        }

        String nameFromRegionLocation = MCAUtil.createNameFromRegionLocation(regionX, regionZ);
        Path filePath = Paths.get(config.getOutputRegionDirectory().getPath(), nameFromRegionLocation);
        MCAUtil.writeMCAFile(region, filePath.toFile(), true);
    }

    private Chunk renderChunk(ChunkInfoMap chunkInfoMap) {

        Chunk chunk = Chunk.newChunk();

        int seaLevel = config.getSeaLevel();

        for (int z = 0; z < Constants.CHUNK_LEN_Z; z++) {
            for (int x = 0; x < Constants.CHUNK_LEN_X; x++) {
                byte terrainIndex = chunkInfoMap.getTerrainIndex(x, z);
                byte surfaceIndex = chunkInfoMap.getSurfaceIndex(x, z);
                byte mountainIndex = chunkInfoMap.getMountainIndex(x, z);
                TerrainCsvContent.Record terrainRecord = terrainCsvContent.getByColorIndex(terrainIndex);
                SurfaceCsvContent.Record surfaceRecord = surfaceCsvContent.getByColorIndex(surfaceIndex);
                int levelOffset = terrainRecord != null ? terrainRecord.getLevel() : terrainIndex - seaLevel;
                String blockId = surfaceRecord != null ? surfaceRecord.getBlockId() : "dirt";

                byte surfaceDepth = surfaceRecord != null ? surfaceRecord.getDepth() : 1;
                int top = seaLevel + levelOffset + mountainIndex;
                CompoundTag block = worldRepo.getBlockCompoundId(blockId);

                int currentLevel = 0;
                if (levelOffset < 0) { // under water
                    int stoneCount = Math.max(0, top - surfaceDepth);
                    int surfaceCount = Math.max(0, top - stoneCount);
                    int waterDepth = Math.abs(levelOffset);

                    currentLevel = buildStoneStack(currentLevel, chunk, x, z, stoneCount + config.getBaseLevel());
                    currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceCount, block);
                    currentLevel = buildWaterStack(currentLevel, chunk, x, z, waterDepth);
                } else { // above water
                    int stoneCount = Math.max(0, top - surfaceDepth);
                    int surfaceCount = Math.max(0, top - stoneCount);

                    currentLevel = buildStoneStack(currentLevel, chunk, x, z, stoneCount + config.getBaseLevel());
                    currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceCount, block);

                    // additional block, e.g. a sapling

                    if (surfaceRecord != null && surfaceRecord.hasAdditionalBlock()) {
                        float frequency = surfaceRecord.getAdditionalBlockFrequency();
                        if (frequency >= 1f || Math.random() <= frequency) {
                            CompoundTag additionalBlock = worldRepo.getBlockCompoundId(surfaceRecord.getAdditionalBlockId());
                            currentLevel = buildAdditionalBlock(currentLevel, chunk, x, z, additionalBlock);
                        }
                    }
                }
            }
        }

        chunk.cleanupPalettesAndBlockStates();
        return chunk;
    }

    private int buildAdditionalBlock(int currentLevel, Chunk chunk, int x, int z, CompoundTag additionalCompound) {
        chunk.setBlockStateAt(x - config.getOffsetX(), currentLevel, z - config.getOffsetZ(), additionalCompound, false);
        return ++currentLevel;
    }

    private int buildWaterStack(int startLevel, Chunk chunk, int x, int z, int waterDepthCount) {
        int currentLevel = startLevel;
        for (int i = 0; i < waterDepthCount; i++) {
            chunk.setBlockStateAt(x - config.getOffsetX(), currentLevel, z - config.getOffsetZ(), waterCompound, false);
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildSurfaceStack(int startLevel, Chunk chunk, int x, int z, int landSurfaceCount, CompoundTag surfaceBlock) {
        int currentLevel = startLevel;
        for (int i = 0; i < landSurfaceCount; i++) {
            chunk.setBlockStateAt(x - config.getOffsetX(), currentLevel, z - config.getOffsetZ(), surfaceBlock, false);
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildStoneStack(int startLevel, Chunk chunk, int x, int z, int stoneCount) {
        int currentLevel = startLevel;
        for (int i = 0; i < stoneCount; i++) {
            if (i == 0) {
                chunk.setBlockStateAt(x - config.getOffsetX(), currentLevel, z - config.getOffsetZ(), bedrockCompound, false);
            } else if (!config.getEmptyLevels().contains(currentLevel)) {
                chunk.setBlockStateAt(x - config.getOffsetX(), currentLevel, z - config.getOffsetZ(), stoneCompound, false);
            }
            currentLevel++;
        }
        return currentLevel;
    }
}
