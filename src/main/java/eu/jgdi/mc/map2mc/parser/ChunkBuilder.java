package eu.jgdi.mc.map2mc.parser;

import java.util.HashMap;
import java.util.Map;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.BlockLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.raw.ChunkInfoMap;
import eu.jgdi.mc.map2mc.model.raw.WorldRaster;

public class ChunkBuilder {

    private final ChunkLocation chunkLocation;

    private final byte[][] terrainIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private final byte[][] surfaceIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private final byte[][] mountainIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private final byte[][] biomeIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    // maps each location to how many times there has been value insertions
    private Map<BlockLocation, Integer> insertions = new HashMap<>();

    private ChunkInfoMap chunkInfoMap = null;

    public ChunkBuilder(ChunkLocation chunkLocation) {
        this.chunkLocation = chunkLocation;
    }

    public ChunkLocation getChunkLocation() {
        return chunkLocation;
    }

    /**
     * Checks whether all locations have been assigned a value at least once
     */
    public boolean isComplete() {
        return insertions.size() == Constants.CHUNK_LEN_X * Constants.CHUNK_LEN_Z;
    }

    public boolean isIncomplete() {
        return !isComplete();
    }

    public void insert(BlockLocation blockLocation, WorldRaster.Info info) {
        // add the value; we'll get the average of all additions later
        int z = blockLocation.getZ(ReferenceFrame.CHUNK);
        int x = blockLocation.getX(ReferenceFrame.CHUNK);
        z = Math.abs(z);
        x = Math.abs(x);
        terrainIndexField[z][x] = info.getTerrainIndex();
        surfaceIndexField[z][x] = info.getSurfaceIndex();
        mountainIndexField[z][x] = info.getMountainIndex();
        biomeIndexField[z][x] = info.getBiomeIndex();

        Integer ins = insertions.computeIfAbsent(blockLocation, key -> 0);
        insertions.put(blockLocation, ins + 1);
    }

    /**
     * Compiles the chunk surface and returns it if all data is ready.
     * Otherwise throws exception.
     * <br>
     * The surface is organized as [y][x] to provide cache-optimizations
     * when iterating by x first (ie line by line).
     *
     * @return The chunk height map
     *
     * @throws Exception The chunk has blocks that haven't been inserted
     */
    public ChunkInfoMap build() throws Exception {

        if (isIncomplete()) {
            throw new Exception("cannot compile chunk surface; chunk has missing data");
        }

        if (chunkInfoMap == null) {
            chunkInfoMap = new ChunkInfoMap(
                    chunkLocation,
                    terrainIndexField,
                    surfaceIndexField,
                    mountainIndexField,
                    biomeIndexField);
        }

        return chunkInfoMap;
    }
}
