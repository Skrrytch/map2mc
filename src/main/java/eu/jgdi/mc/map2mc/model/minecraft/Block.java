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
            "coarse_dirt",
            "cobblestone",
            "cracked_stone_bricks",
            "cut_red_sandstone",
            "dirt",
            "granite",
            "grass_block",
            "grass_path",
            "gravel",
            "mossy_stone_bricks",
            "stone_bricks",
            "quartz_block",
            "red_sand",
            "sand",
            "sandstone",
            "red_sandstone",
            "smooth_red_sandstone",
            "smooth_sandstone",
            "stone",
            "water",
            // Items:
            "acacia_sapling",
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
            "birch_log",
            "dead_bush",
            "dark_oak_sapling",
            "fern",
            "grass",
            "lantern",
            "lily_of_the_valley",
            "oak_sapling",
            "oak_log",
            "orange_tulip",
            "oxeye_daisy",
            "poppy",
            "pufferfish",
            "pufferfish_bucket",
            "pufferfish_spawn_egg",
            "rose_bush",
            "sea_grass",
            "spruce_sapling",
            "sweet_berries",
            "tall_grass",
            "white_tulip"
    );
}
