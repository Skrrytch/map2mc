package eu.jgdi.mc.map2mc.config;

import java.util.HashMap;
import java.util.Map;

import net.querz.nbt.CompoundTag;

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
}
