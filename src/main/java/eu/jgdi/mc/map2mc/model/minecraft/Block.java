package eu.jgdi.mc.map2mc.model.minecraft;

import java.util.Map;

import static java.util.Map.entry;

public enum Block {

    DIRT("dirt"),
    STONE("stone"),
    UNKNOWN("gold_block"),
    WATER("water"),
    BEDROCK("bedrock"),
    OAK_LOG("oak_log");

    private String blockId;

    Block(String id) {
        this.blockId = id;
    }

    public String getBlockId() {
        return blockId;
    }

    public static final Map<String, Integer> EXPECTED_BLOCK_TYPES = Map.ofEntries(
            // Blocks:
            entry("andesite", 1),
            entry("bricks", 1),
            entry("clay_block", 1),
            entry("coarse_dirt", 1),
            entry("cobblestone", 1),
            entry("cracked_stone_bricks", 1),
            entry("cut_red_sandstone", 1),
            entry("dirt", 1),
            entry("granite", 1),
            entry("grass_block", 1),
            entry("grass_path", 1),
            entry("gravel", 1),
            entry("mossy_stone_bricks", 1),
            entry("stone_bricks", 1),
            entry("quartz_block", 1),
            entry("quartz_pillar", 1),
            entry("smooth_quartz", 1),
            entry("red_sand", 1),
            entry("sand", 1),
            entry("sandstone", 1),
            entry("red_sandstone", 1),
            entry("smooth_red_sandstone", 1),
            entry("smooth_sandstone", 1),
            entry("snow_block", 1),
            entry("stone", 1),
            entry("water", 1),
            // Concrete:
            entry("white_concrete", 1),
            entry("white_concrete_powder", 1),
            entry("yellow_concrete", 1),
            entry("yellow_concrete_powder", 1),
            entry("black_concrete", 1),
            entry("black_concrete_powder", 1),
            entry("cyan_concrete", 1),
            entry("cyan_concrete_powder", 1),
            entry("gray_concrete", 1),
            entry("gray_concrete_powder", 1),
            entry("green_concrete", 1),
            entry("green_concrete_powder", 1),
            entry("light_blue_concrete", 1),
            entry("light_blue_concrete_powder", 1),
            entry("magenta_concrete", 1),
            entry("magenta_concrete_powder", 1),
            entry("red_concrete", 1),
            entry("red_concrete_powder", 1),
            // Items:
            entry("acacia_sapling", 1),
            entry("azure_bluet", 1),
            entry("allium", 1),
            entry("cactus", 1),
            entry("bamboo", 1),
            entry("blue_orchid", 1),
            entry("brain_coral", 1),
            entry("brain_coral_fan", 1),
            entry("brown_mushroom", 1),
            entry("bubble_coral", 1),
            entry("bubble_coral_fan", 1),
            entry("cornflower", 1),
            entry("birch_sapling", 1),
            entry("birch_log", 1),
            entry("dead_bush", 1),
            entry("dark_oak_sapling", 1),
            entry("fern", 1),
            entry("grass", 1),
            entry("jungle_sapling", 1),
            entry("jungle_log", 1),
            entry("lantern", 1),
            entry("lily_of_the_valley", 1),
            entry("melon", 1),
            entry("oak_sapling", 1),
            entry("oak_log", 1),
            entry("orange_tulip", 1),
            entry("oxeye_daisy", 1),
            entry("poppy", 1),
            entry("pufferfish", 1),
            entry("pufferfish_bucket", 1),
            entry("pufferfish_spawn_egg", 1),
            entry("pumpkin", 1),
            entry("red_mushroom", 1),
            entry("rose_bush", 1),
            entry("sea_grass", 1),
            entry("spruce_sapling", 1),
            entry("sweet_berries", 1),
            entry("tall_grass", 1),
            entry("white_tulip", 1)
    );
}
