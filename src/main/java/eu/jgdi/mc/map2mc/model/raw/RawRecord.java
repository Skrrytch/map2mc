package eu.jgdi.mc.map2mc.model.raw;

import eu.jgdi.mc.map2mc.config.Constants;

public class RawRecord {

    public static final int SIZE = 5;

    public static void putFlag(byte[] bytes, int z, int x, byte data) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        bytes[offset] = data;
    }

    public static void putTerrainIndex(byte[] bytes, int z, int x, byte data) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        bytes[offset + 1] = data;
    }

    public static void putSurfaceIndex(byte[] bytes, int z, int x, byte data) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        bytes[offset + 2] = data;
    }

    public static void putMountainIndex(byte[] bytes, int z, int x, byte data) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        bytes[offset + 3] = data;
    }

    public static void putBiomeIndex(byte[] bytes, int z, int x, byte data) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        bytes[offset + 4] = data;
    }

    public static byte getFlag(byte[] rawData, int x, int z) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        return rawData[offset];
    }

    public static byte getTerrainIndex(byte[] rawData, int x, int z) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        return rawData[offset + 1];
    }

    public static byte getSurfaceIndex(byte[] rawData, int x, int z) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        return rawData[offset + 2];
    }

    public static byte getMountainIndex(byte[] rawData, int x, int z) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        return rawData[offset + 3];
    }
    public static byte getBiomeIndex(byte[] rawData, int x, int z) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        return rawData[offset + 4];
    }
}
