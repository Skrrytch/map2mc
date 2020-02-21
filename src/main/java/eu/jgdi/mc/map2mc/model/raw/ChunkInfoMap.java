package eu.jgdi.mc.map2mc.model.raw;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;

public class ChunkInfoMap {

    public static final int DATA_SZ = RawRecord.SIZE * Constants.CHUNK_LEN_X * Constants.CHUNK_LEN_Z;

    private final byte[][] flagField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private byte[][] terrainIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private byte[][] surfaceIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private byte[][] mountainIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private byte[][] biomeIndexField = new byte[Constants.CHUNK_LEN_Z][Constants.CHUNK_LEN_X];

    private ChunkLocation location;

    public ChunkInfoMap(
            ChunkLocation location,
            byte[][] terrainIndexField,
            byte[][] surfaceIndexField,
            byte[][] mountainIndexField,
            byte[][] biomeIndexField) {
        this.location = location;
        this.terrainIndexField = terrainIndexField;
        this.surfaceIndexField = surfaceIndexField;
        this.mountainIndexField = mountainIndexField;
        this.biomeIndexField = biomeIndexField;
    }

    public ChunkInfoMap(ChunkLocation location, byte[] rawData) {

        this.location = location;

        for (int z = 0; z < Constants.CHUNK_LEN_Z; z++) {
            for (int x = 0; x < Constants.CHUNK_LEN_X; x++) {
                flagField[z][x] = RawRecord.getFlag(rawData, x, z);
                terrainIndexField[z][x] = RawRecord.getTerrainIndex(rawData, x, z);
                surfaceIndexField[z][x] = RawRecord.getSurfaceIndex(rawData, x, z);
                mountainIndexField[z][x] = RawRecord.getMountainIndex(rawData, x, z);
                biomeIndexField[z][x] = RawRecord.getBiomeIndex(rawData, x, z);
            }
        }
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public int getTerrainIndex(int x, int z) {
        return terrainIndexField[z][x] & 0xFF;
    }

    public int getSurfaceIndex(int x, int z) {
        return surfaceIndexField[z][x] & 0xFF;
    }

    public int getBiomeIndex(int x, int z) {
        return biomeIndexField[z][x] & 0xFF;
    }

    public int getMountainLevel(int x, int z) {
        return mountainIndexField[z][x] & 0xFF;
    }

    public int getFlagField(int x, int z) {
        return flagField[z][x] & 0xFF;
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[DATA_SZ];

        for (int z = 0; z < Constants.CHUNK_LEN_Z; z++) {
            for (int x = 0; x < Constants.CHUNK_LEN_X; x++) {
                RawRecord.putFlag(bytes, z, x, (byte) 1);
                RawRecord.putTerrainIndex(bytes, z, x, terrainIndexField[z][x]);
                RawRecord.putSurfaceIndex(bytes, z, x, surfaceIndexField[z][x]);
                RawRecord.putMountainIndex(bytes, z, x, mountainIndexField[z][x]);
                RawRecord.putBiomeIndex(bytes, z, x, biomeIndexField[z][x]);
            }
        }

        return bytes;
    }
}
