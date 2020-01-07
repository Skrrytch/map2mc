package eu.jgdi.mc.map2mc.model.minecraft;

import java.util.Set;

public enum Block {

    DIRT("dirt"),
    STONE("stone"),
    UNKNOWN("gold_block"),
    WATER("water"),
    BEDROCK("bedrock");

    private String blockId;

    Block(String id) {
        this.blockId = id;
    }

    public String getBlockId() {
        return blockId;
    }

    public static final Set<String> EXPECTED_BLOCK_TYPES = Set.of(
            // Blocks:
            "dirt",
            "granite",
            "grass_block",
            "grass_path",
            "red_sand",
            "sand",
            "stone",
            "water",
            // Items:
            "lantern",
            "oak_sapling",
            "spruce_sapling",
            "birch_sapling",
            "acacia_sapling",
            "cornflower",
            "grass",
            "orange_tulip",
            "oxeye_daisy",
            "poppy",
            "rose_bush",
            "tall_grass"
    );
}
