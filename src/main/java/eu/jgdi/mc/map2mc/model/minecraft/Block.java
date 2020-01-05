package eu.jgdi.mc.map2mc.model.minecraft;

public enum Block {

    DIRT("dirt"),
    STONE("stone"),
    UNKNOWN("gold_block"),
    WATER("water");

    private String blockId;

    Block(String id) {
        this.blockId = id;
    }

    public String getBlockId() {
        return blockId;
    }
}
