package eu.jgdi.mc.map2mc.model.raw;

import eu.jgdi.mc.map2mc.config.Constants;

public class RawRecord {

    public static final int SIZE = 2;

    public static void putTerrainIndex(byte[] bytes, int z, int x, byte data) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        bytes[offset] = data;
    }

    public static void putSurfaceIndex(byte[] bytes, int z, int x, byte data) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        bytes[offset + 1] = data;
    }

    public static byte getTerrainIndex(byte[] rawData, int x, int z) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        return rawData[offset];
    }

    public static byte getSurfaceIndex(byte[] rawData, int x, int z) {
        int offset = SIZE * (x + z * Constants.CHUNK_LEN_Z);
        return rawData[offset + 1];
    }
}
