package eu.jgdi.mc.map2mc.renderer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
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

    private final long fileNumber;

    private Set<String> unexpectedBlockTypes = new HashSet<>();

    private Set<String> messages = new HashSet<>();

    private static final Logger logger = Logger.logger();

    private final CountDownLatch latch;

    private final CompoundTag waterCompound;

    private final CompoundTag stoneCompound;

    private final CompoundTag unknownCompound;

    private final CompoundTag bedrockCompound;

    private String fileName;

    private RegionInfoMap regionInfoMap;

    private WorldConfig config;

    private WorldRepository worldRepo;

    private TerrainCsvContent terrainCsvContent;

    private SurfaceCsvContent surfaceCsvContent;

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
        this.waterCompound = worldRepo.getBlockCompoundId(Block.WATER.getBlockId());
        this.stoneCompound = worldRepo.getBlockCompoundId(Block.STONE.getBlockId());
        this.unknownCompound = worldRepo.getBlockCompoundId(Block.UNKNOWN.getBlockId());
        this.bedrockCompound = worldRepo.getBlockCompoundId(Block.BEDROCK.getBlockId());
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

        int regionX = regionInfoMap.getLocation().getX();
        int regionZ = regionInfoMap.getLocation().getZ();
        MCAFile region = new MCAFile(regionX, regionZ);

        long chunkSize = Constants.REGION_LEN_Z * Constants.REGION_LEN_X;
        long chunkCount = 0;
        for (int z = 0; z < Constants.REGION_LEN_Z; z++) {
            for (int x = 0; x < Constants.REGION_LEN_X; x++) {
                chunkCount++;
                ChunkLocation location = new ChunkLocation(x, z, ReferenceFrame.REGION);
                ChunkInfoMap chunkInfoMap = regionInfoMap.getChunk(location);

                Chunk chunk = renderChunk(chunkInfoMap);

                region.setChunk(x, z, chunk);
                if (chunkCount % 256 == 0) {
                    logger.info(
                            "  Region file {0} (''{1}''): Chunk {2} of {3} done",
                            fileNumber,
                            fileName,
                            chunkCount,
                            chunkSize);
                }
            }
        }

        String nameFromRegionLocation = MCAUtil.createNameFromRegionLocation(regionX, regionZ);
        Path filePath = Paths.get(config.getOutputRegionDirectory().getPath(), nameFromRegionLocation);
        MCAUtil.writeMCAFile(region, filePath.toFile(), true);
    }

    private Chunk renderChunk(ChunkInfoMap chunkInfoMap) {

        Chunk chunk = Chunk.newChunk();

        int baseLevel = config.getBaseLevel();
        int seaLevel = config.getSeaLevel();

        for (int z = 0; z < Constants.CHUNK_LEN_Z; z++) {
            for (int x = 0; x < Constants.CHUNK_LEN_X; x++) {
                byte flag = chunkInfoMap.getFlagField(x, z);
                boolean undefined = (flag == 0);
                if (undefined) {
                    // We have no information about the blocks but the region is not finished
                    // In this case we build standard stone blocks of baselevel height and water blocks of seaLevel height
                    int currentLevel = 0;
                    currentLevel = buildStoneStack(currentLevel, chunk, x, z, baseLevel);
                    currentLevel = buildWaterStack(currentLevel, chunk, x, z, seaLevel);
                    continue;
                }
                byte terrainIndex = chunkInfoMap.getTerrainIndex(x, z);
                byte surfaceIndex = chunkInfoMap.getSurfaceIndex(x, z);
                byte mountainLevel = chunkInfoMap.getMountainIndex(x, z);
                TerrainCsvContent.Record terrainRecord = terrainCsvContent.getByColorIndex(terrainIndex);
                SurfaceCsvContent.Record surfaceRecord = surfaceCsvContent.getByColorIndex(surfaceIndex);
                int levelOffset = terrainRecord != null ? terrainRecord.getLevel() : terrainIndex - seaLevel;
                String blockId = surfaceRecord != null ? surfaceRecord.getBlockId() : "dirt";

                int surfaceDepth = surfaceRecord != null ? surfaceRecord.getDepth() : 1;
                int top = seaLevel + levelOffset + mountainLevel;
                CompoundTag block = worldRepo.getBlockCompoundId(blockId);

                if (config.validateBlockTypes() && !Block.EXPECTED_BLOCK_TYPES.contains(blockId)) {
                    unexpectedBlockTypes.add(blockId);
                }

                int currentLevel = 0;
                if (levelOffset < 0) { // under water
                    int stoneCount = Math.max(0, top - surfaceDepth);
                    int surfaceCount = Math.max(0, top - stoneCount);
                    int waterDepth = Math.abs(levelOffset);

                    currentLevel = buildStoneStack(currentLevel, chunk, x, z, stoneCount + baseLevel);
                    currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceCount, block);
                    currentLevel = buildWaterStack(currentLevel, chunk, x, z, waterDepth);
                } else { // above water
                    buildStoneStack(currentLevel, chunk, x, z, 15);

                    int stoneCount = Math.max(0, top - surfaceDepth);
                    int surfaceCount = Math.max(0, top - stoneCount);

                    currentLevel = buildStoneStack(currentLevel, chunk, x, z, stoneCount + baseLevel);
                    currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceCount, block);

                    // additional block, e.g. a sapling

                    if (surfaceRecord != null && surfaceRecord.hasAdditionalBlock()) {
                        float frequency = surfaceRecord.getAdditionalBlockFrequency();
                        String additionalBlockId = surfaceRecord.getAdditionalBlockId();
                        if (config.validateBlockTypes()) {
                            validateAdditionalBlock(additionalBlockId, frequency);
                        }
                        if (frequency >= 1f || Math.random() <= frequency) {
                            if (config.validateBlockTypes() && !Block.EXPECTED_BLOCK_TYPES.contains(additionalBlockId)) {
                                unexpectedBlockTypes.add(additionalBlockId);
                            }
                            CompoundTag additionalBlock = worldRepo.getBlockCompoundId(additionalBlockId);
                            currentLevel = buildAdditionalBlock(currentLevel, chunk, x, z, additionalBlock);
                        }
                    }
                }
            }
        }

        chunk.cleanupPalettesAndBlockStates();
        return chunk;
    }

    private void validateAdditionalBlock(String additionalBlockId, float frequency) {
        if (additionalBlockId.contains("sapling") && frequency > 0.5) {
            messages.add("Frequency of a sapling is higher than 50%. That seems to be alot!");
        }
    }

    private int buildAdditionalBlock(int currentLevel, Chunk chunk, int x, int z, CompoundTag additionalCompound) {
        chunk.setBlockStateAt(x, currentLevel, z, additionalCompound, false);
        return ++currentLevel;
    }

    private int buildWaterStack(int startLevel, Chunk chunk, int x, int z, int waterDepthCount) {
        int currentLevel = startLevel;
        for (int i = 0; i < waterDepthCount; i++) {
            chunk.setBlockStateAt(x, currentLevel, z, waterCompound, false);
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildSurfaceStack(int startLevel, Chunk chunk, int x, int z, int landSurfaceCount, CompoundTag surfaceBlock) {
        int currentLevel = startLevel;
        for (int i = 0; i < landSurfaceCount; i++) {
            chunk.setBlockStateAt(x, currentLevel, z, surfaceBlock, false);
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildStoneStack(int startLevel, Chunk chunk, int x, int z, int stoneCount) {
        int currentLevel = startLevel;
        for (int i = 0; i < stoneCount; i++) {
            if (i == 0) {
                chunk.setBlockStateAt(x, currentLevel, z, bedrockCompound, false);
            } else if (!config.getEmptyLevels().contains(currentLevel)) {
                chunk.setBlockStateAt(x, currentLevel, z, stoneCompound, false);
            }
            currentLevel++;
        }
        return currentLevel;
    }

    public Set<String> getUnexpectedBlockTypes() {
        return unexpectedBlockTypes;
    }

    public Set<String> getMessages() {
        return messages;
    }
}
