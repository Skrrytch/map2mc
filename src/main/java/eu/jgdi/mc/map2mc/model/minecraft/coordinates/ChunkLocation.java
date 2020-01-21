package eu.jgdi.mc.map2mc.model.minecraft.coordinates;

import java.util.Map;

import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.FrameTransition;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrameShifter;
import eu.jgdi.mc.map2mc.model.raw.Tuple;

public class ChunkLocation extends MinecraftLocation {

    public ChunkLocation(int x, int z) {
        super(x, z);
    }

    public ChunkLocation(int x, int z, ReferenceFrame referenceFrame) {
        super(x, z, referenceFrame);
    }

    private static Map<FrameTransition, ReferenceFrameShifter> referenceShifters;

    static {
        referenceShifters = Map.of(
                new FrameTransition(ReferenceFrame.WORLD, ReferenceFrame.REGION),
                ChunkLocation::worldToRegionReferenceShifter);
    }

    @Override
    Map<FrameTransition, ReferenceFrameShifter> getReferenceShifters() {
        return referenceShifters;
    }

    private static Tuple<MinecraftLocation> worldToRegionReferenceShifter(MinecraftLocation instance) {
        int chunkX = instance.x % Constants.REGION_LEN_X;
        int chunkZ = instance.z % Constants.REGION_LEN_Z;

        int regionX = instance.x / Constants.REGION_LEN_X;
        int regionZ = instance.z / Constants.REGION_LEN_Z;

        return new Tuple<>(
                new ChunkLocation(chunkX, chunkZ, ReferenceFrame.REGION),
                new RegionLocation(regionX, regionZ)
        );
    }
}
