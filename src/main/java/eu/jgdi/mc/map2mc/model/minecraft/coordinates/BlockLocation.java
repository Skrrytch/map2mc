package eu.jgdi.mc.map2mc.model.minecraft.coordinates;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.FrameTransition;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrameShifter;
import eu.jgdi.mc.map2mc.model.raw.Tuple;

public class BlockLocation extends MinecraftLocation {

    public BlockLocation(int x, int z) {
        super(x, z);
    }

    public BlockLocation(int x, int z, ReferenceFrame referenceFrame) {
        super(x, z, referenceFrame);
    }

    private static Map<FrameTransition, ReferenceFrameShifter> referenceShifters;

    static {
        referenceShifters = Map.of(
                new FrameTransition(ReferenceFrame.WORLD, ReferenceFrame.CHUNK),
                BlockLocation::worldToChunkReferenceShifter);
    }

    @Override
    Map<FrameTransition, ReferenceFrameShifter> getReferenceShifters() {
        return referenceShifters;
    }

    private static Tuple<MinecraftLocation> worldToChunkReferenceShifter(MinecraftLocation instance) {
        int blockX = instance.x % Constants.CHUNK_LEN_X;
        int blockZ = instance.z % Constants.CHUNK_LEN_Z;

        int chunkX = instance.x / Constants.CHUNK_LEN_X;
        int chunkZ = instance.z / Constants.CHUNK_LEN_Z;

        return new Tuple<>(
                new BlockLocation(blockX, blockZ, ReferenceFrame.CHUNK),
                new ChunkLocation(chunkX, chunkZ)
        );
    }
}
