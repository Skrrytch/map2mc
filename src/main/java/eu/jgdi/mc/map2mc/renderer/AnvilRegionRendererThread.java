package eu.jgdi.mc.map2mc.renderer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.mca.Chunk;
import net.querz.nbt.mca.MCAFile;
import net.querz.nbt.mca.MCAUtil;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.config.csv.BiomesCsvContent;
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

    private final static List<String> DEFAULT_BLOCK_IDS = List.of("dirt");

    private final long fileNumber;

    private final Set<String> messages = new HashSet<>();

    private final CountDownLatch latch;

    private final CompoundTag waterCompound;

    private final CompoundTag stoneCompound;

    private final CompoundTag bedrockCompound;

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
        this.waterCompound = worldRepo.getBlockCompoundId(Block.WATER.getBlockId());
        this.stoneCompound = worldRepo.getBlockCompoundId(Block.STONE.getBlockId());
        this.bedrockCompound = worldRepo.getBlockCompoundId(Block.BEDROCK.getBlockId());
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
                boolean undefined = (chunkInfoMap.getFlagField(x, z) == 0);
                if (undefined) {
                    level = buildBlocksForUndefined(z, x, chunk);
                } else {
                    level = buildBlocks(z, x, chunkInfoMap, chunk);
                }
                int worldX = relRegionX + relChunkX + x;
                int worldZ = relRegionZ + relChunkZ + z;
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

    private int buildBlocks(int z, int x, ChunkInfoMap chunkInfoMap, Chunk chunk) {
        int level;
        int currentLevel = 0;
        byte terrainIndex = chunkInfoMap.getTerrainIndex(x, z);
        byte surfaceIndex = chunkInfoMap.getSurfaceIndex(x, z);
        byte mountainLevel = chunkInfoMap.getMountainLevel(x, z);
        byte biomeIndex = chunkInfoMap.getBiomeIndex(x, z);

        BiomesCsvContent.Record biomeRecord = biomeCsvContent.getByColorIndex(biomeIndex);
        TerrainCsvContent.Record terrainRecord = terrainCsvContent.getByColorIndex(terrainIndex);
        SurfaceCsvContent.Record surfaceRecord = surfaceCsvContent.getByColorIndex(
                surfaceIndex,
                biomeRecord != null ? biomeRecord.getBiomeName() : null);

        int terrainLevelOffset = terrainRecord != null ? terrainRecord.getLevel() : terrainIndex - seaLevel;
        List<String> surfaceBlockIds = surfaceRecord != null ? surfaceRecord.getBlockIds() : DEFAULT_BLOCK_IDS;
        int surfaceDepth = surfaceRecord != null ? surfaceRecord.getDepth() : 1;

        int topLevel = seaLevel + terrainLevelOffset + mountainLevel;

        List<CompoundTag> surfaceBlocks = surfaceBlockIds.stream()
                .map(id -> worldRepo.getBlockCompoundId(id))
                .collect(Collectors.toList());

        CompoundTag additionalBlock = null;
        int additionalBlockCount = 1;
        if (surfaceRecord != null && surfaceRecord.hasAdditionalBlock()) {
            List<SurfaceCsvContent.AdditionalItem> additionalBlocks = surfaceRecord.getAdditionalBlocks();
            Optional<SurfaceCsvContent.AdditionalItem> itemToUse = additionalBlocks.stream()
                    .filter(item -> Math.random() <= item.getFrequency())
                    .findAny();
            if (itemToUse.isPresent()) {
                SurfaceCsvContent.AdditionalItem additionalItem = itemToUse.get();
                String additionalBlockId = additionalItem.getBlockId();
                Integer count = Block.EXPECTED_BLOCK_TYPES.get(additionalBlockId);
                additionalBlock = worldRepo.getBlockCompoundId(additionalBlockId);
                additionalBlockCount = count != null ? count : 1;
            }
        }

        if (biomeRecord != null) {
            chunk.setBiomeAt(x, z, biomeRecord.getBiomeId()); // jungle
        }
        if (terrainLevelOffset < 0) { // under water
            int stoneCount = Math.max(0, topLevel - surfaceDepth * surfaceBlockIds.size());
            int waterDepth = Math.abs(terrainLevelOffset);

            currentLevel = buildStoneStack(currentLevel, chunk, x, z, stoneCount + baseLevel);
            currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceDepth, surfaceBlocks);
            currentLevel = buildWaterStack(currentLevel, chunk, x, z, waterDepth);
            //                    if (additionalBlock != null) {no

            //                        setAdditionalBlock(currentLevel, chunk, x, z, additionalBlock);
            //                    }
        } else { // above water
            buildStoneStack(currentLevel, chunk, x, z, 15);

            int stoneCount = Math.max(0, topLevel - surfaceDepth * surfaceBlockIds.size());

            currentLevel = buildStoneStack(currentLevel, chunk, x, z, stoneCount + baseLevel);
            currentLevel = buildSurfaceStack(currentLevel, chunk, x, z, surfaceDepth, surfaceBlocks);

            // additional block, e.g. a sapling

            if (additionalBlock != null) {
                currentLevel = buildAdditionalBlock(currentLevel, chunk, x, z, additionalBlock, additionalBlockCount);
            }
        }
        level = currentLevel;
        return level;
    }

    private int buildBlocksForUndefined(int z, int x, Chunk chunk) {
        int currentLevel = 0;
        // We have no information about the blocks but the region is not finished
        // In this case we build standard stone blocks of baselevel height and water blocks of seaLevel height
        currentLevel = buildStoneStack(currentLevel, chunk, x, z, baseLevel);
        currentLevel = buildWaterStack(currentLevel, chunk, x, z, seaLevel);
        return currentLevel;
    }

    private int buildAdditionalBlock(
            int currentLevel,
            Chunk chunk,
            int x,
            int z,
            CompoundTag additionalCompound,
            int additionalBlockCount) {
        for (int i = 0; i < additionalBlockCount; i++) {
            chunk.setBlockStateAt(x, currentLevel, z, additionalCompound, false);
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildWaterStack(int startLevel, Chunk chunk, int x, int z, int waterDepthCount) {
        int currentLevel = startLevel;
        for (int i = 0; i < waterDepthCount; i++) {
            chunk.setBlockStateAt(x, currentLevel, z, waterCompound, false);
            currentLevel++;
        }
        return currentLevel;
    }

    private int buildSurfaceStack(
            int startLevel, Chunk chunk, int x, int z, int landSurfaceCount,
            List<CompoundTag> surfaceBlocks) {
        int currentLevel = startLevel;
        for (int i = 0; i < landSurfaceCount; i++) {
            for (CompoundTag surfaceBlock : surfaceBlocks) {
                chunk.setBlockStateAt(x, currentLevel, z, surfaceBlock, false);
                currentLevel++;
            }
        }
        return currentLevel;
    }

    private int buildStoneStack(int startLevel, Chunk chunk, int x, int z, int stoneCount) {
        int currentLevel = startLevel;
        for (int i = 0; i < stoneCount; i++) {
            if (stoneCount > 3 && i < 3) {
                chunk.setBlockStateAt(x, currentLevel, z, bedrockCompound, false);
            } else {
                chunk.setBlockStateAt(x, currentLevel, z, stoneCompound, false);
            }
            currentLevel++;
        }
        return currentLevel;
    }

    public Set<String> getMessages() {
        return messages;
    }
}
