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
            "andesite",
            "bricks",
            "clay_block",
            "cobblestone",
            "cut_red_sandstone",
            "dirt",
            "granite",
            "grass_block",
            "grass_path",
            "gravel",
            "quartz_block",
            "red_sand",
            "sand",
            "smooth_red_sandstone",
            "smooth_sandstone",
            "stone",
            "water",
            // Items:
            "acacia_sapling",
            "acacia_leaves",
            "azure_bluet",
            "allium",
            "cactus",
            "bamboo",
            "blue_orchid",
            "brain_coral",
            "brain_coral_fan",
            "bubble_coral",
            "bubble_coral_fan",
            "cornflower",
            "birch_sapling",
            "birch_leaves",
            "birch_log",
            "dead_bush",
            "fern",
            "grass",
            "lantern",
            "lily_of_the_valley",
            "oak_sapling",
            "oak_leaves",
            "oak_log",
            "orange_tulip",
            "oxeye_daisy",
            "poppy",
            "rose_bush",
            "sea_grass",
            "spruce_sapling",
            "spruce_leaves",
            "sweet_berries",
            "tall_grass",
            "white_tulip"
    );
}
