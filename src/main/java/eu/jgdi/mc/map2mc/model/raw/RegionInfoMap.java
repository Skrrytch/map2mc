package eu.jgdi.mc.map2mc.model.raw;

import java.util.Arrays;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.RegionLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;

public class RegionInfoMap {

    public static final int DATA_SZ = ChunkInfoMap.DATA_SZ * Constants.REGION_LEN_X * Constants.REGION_LEN_Z;

    RegionLocation location;

    ChunkInfoMap[][] data = new ChunkInfoMap[Constants.REGION_LEN_Z][Constants.REGION_LEN_X];

    private RegionInfoMap() {
    }

    public RegionInfoMap(RegionLocation location, byte[] data) throws Exception {

        if (data.length != DATA_SZ) {
            throw new Exception("input region data length invalid");
        }

        this.location = location;

        for (int z = 0; z < Constants.REGION_LEN_Z; z++) {
            for (int x = 0; x < Constants.REGION_LEN_X; x++) {

                ChunkLocation chunkLocation = new ChunkLocation(x, z, ReferenceFrame.REGION);
                int bufPtr = ((Constants.REGION_LEN_Z * z) + x) * ChunkInfoMap.DATA_SZ;

                ChunkInfoMap chunk = new ChunkInfoMap(
                        chunkLocation,
                        Arrays.copyOfRange(data, bufPtr, bufPtr + ChunkInfoMap.DATA_SZ));

                insertChunk(chunk);
            }
        }
    }

    public ChunkInfoMap getChunk(ChunkLocation chunkLocation) {
        return this.data[chunkLocation.getZ(ReferenceFrame.REGION)]
                [chunkLocation.getX(ReferenceFrame.REGION)];
    }

    public void insertChunk(ChunkInfoMap chunk) {
        ChunkLocation chunkLocation = chunk.getLocation();

        this.data[chunkLocation.getZ(ReferenceFrame.REGION)]
                [chunkLocation.getX(ReferenceFrame.REGION)] = chunk;
    }

    public RegionLocation getLocation() {
        return location;
    }
}
