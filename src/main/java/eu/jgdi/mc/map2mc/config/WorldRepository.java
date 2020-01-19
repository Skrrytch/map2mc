package eu.jgdi.mc.map2mc.config;

import java.util.HashMap;
import java.util.Map;

import net.querz.nbt.CompoundTag;

import eu.jgdi.mc.map2mc.model.raw.WorldRaster;

public class WorldRepository {

    private WorldConfig config;

    private Map<String, CompoundTag> compoundTags = new HashMap<>();

    public WorldRepository(WorldConfig config) {
        this.config = config;
    }

    public WorldConfig getConfig() {
        return config;
    }

    public CompoundTag getBlockCompoundId(String id) {
        if (!compoundTags.containsKey(id)) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("Name", "minecraft:" + id);
            compoundTags.put(id, compoundTag);
        }
        return compoundTags.get(id);
    }

    private Map<Integer, WorldRaster.Info> rasterInfoRepo = new HashMap<>();

    public WorldRaster.Info buildRasterInfo(
            byte terrainColorIndex,
            byte surfaceColorIndex,
            byte mountainColorIndex,
            byte biomeColorIndex) {
        return new WorldRaster.Info(terrainColorIndex, surfaceColorIndex, mountainColorIndex, biomeColorIndex);
        //        int index = terrainColorIndex * 256 * 256 + surfaceColorIndex * 256 + mountainColorIndex;
        //        WorldRaster.Info info = rasterInfoRepo.get(index);
        //        if (info != null) {
        //            return info;
        //        }
        //        info = new WorldRaster.Info(terrainColorIndex, surfaceColorIndex, mountainColorIndex);
        //        rasterInfoRepo.put(index, info);
        //        return info;
    }
}
