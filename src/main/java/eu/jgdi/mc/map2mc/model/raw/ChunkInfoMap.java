package eu.jgdi.mc.map2mc.model.raw;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;

public class ChunkInfoMap {

    public static final int DATA_SZ = RawRecord.SIZE * Constants.CHUNK_LEN_X * Constants.CHUNK_LEN_Z;

    private byte[][] terrainIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private byte[][] surfaceIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private ChunkLocation location;

    public ChunkInfoMap(ChunkLocation location, byte[][] terrainIndexField, byte[][] surfaceIndexField) {
        this.location = location;
        this.terrainIndexField = terrainIndexField;
        this.surfaceIndexField = surfaceIndexField;
    }

    public ChunkInfoMap(ChunkLocation location, byte[] rawData) {

        this.location = location;

        for (int z = 0; z < Constants.CHUNK_LEN_Z; z++) {
            for (int x = 0; x < Constants.CHUNK_LEN_X; x++) {
                terrainIndexField[z][x] = RawRecord.getTerrainIndex(rawData, x, z);
                surfaceIndexField[z][x] = RawRecord.getSurfaceIndex(rawData, x, z);
            }
        }
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public byte getTerrainIndex(int x, int z) {
        return terrainIndexField[z][x];
    }

    public byte getSurfaceIndex(int x, int z) {
        return surfaceIndexField[z][x];
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[DATA_SZ];

        for (int z = 0; z < Constants.CHUNK_LEN_Z; z++) {
            for (int x = 0; x < Constants.CHUNK_LEN_X; x++) {
                RawRecord.putTerrainIndex(bytes, z, x, terrainIndexField[z][x]);
                RawRecord.putSurfaceIndex(bytes, z, x, surfaceIndexField[z][x]);
            }
        }

        return bytes;
    }
}
