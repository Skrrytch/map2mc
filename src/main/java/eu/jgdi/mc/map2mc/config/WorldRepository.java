package eu.jgdi.mc.map2mc.config;

import java.util.HashMap;
import java.util.Map;

import net.querz.nbt.CompoundTag;

public class WorldRepository {

    private WorldConfig config;

    private int worldRectNorthWestX;

    private int worldRectNorthWestY;

    private int worldRectNorthWestZ;

    private int worldRectSouthEastX;

    private int worldRectSouthEastY;

    private int worldRectSouthEastZ;

    private int worldRectCenterX;

    private int worldRectCenterY;

    private int worldRectCenterZ;

    private int worldRectWidth;

    private int worldRectHeight;

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

    public void setExtends(int mapStartX, int mapStartY, int mapMaxX, int mapMaxY) {
        this.worldRectWidth = mapMaxX - mapStartX;
        this.worldRectHeight = mapMaxY - mapStartY;
        this.worldRectNorthWestX = mapStartX - config.getOriginX();
        this.worldRectNorthWestZ = mapStartY - config.getOriginY();
        this.worldRectSouthEastX = worldRectNorthWestX + worldRectWidth - 1;
        this.worldRectSouthEastZ = worldRectNorthWestZ + worldRectHeight - 1;
        this.worldRectCenterX = worldRectNorthWestX + worldRectWidth / 2;
        this.worldRectCenterZ = worldRectNorthWestZ + worldRectHeight / 2;
    }

    public int getWorldRectNorthWestX() {
        return worldRectNorthWestX;
    }

    public int getWorldRectNorthWestZ() {
        return worldRectNorthWestZ;
    }

    public int getWorldRectSouthEastX() {
        return worldRectSouthEastX;
    }

    public int getWorldRectSouthEastZ() {
        return worldRectSouthEastZ;
    }

    public int getWorldRectCenterX() {
        return worldRectCenterX;
    }

    public int getWorldRectCenterZ() {
        return worldRectCenterZ;
    }

    public int getWorldRectWidth() {
        return worldRectWidth;
    }

    public int getWorldRectHeight() {
        return worldRectHeight;
    }

    public int getWorldRectNorthWestY() {
        return worldRectNorthWestY;
    }

    public int getWorldRectSouthEastY() {
        return worldRectSouthEastY;
    }

    public int getWorldRectCenterY() {
        return worldRectCenterY;
    }

    public void setWorldRectNorthWestY(int worldRectNorthWestY) {
        this.worldRectNorthWestY = worldRectNorthWestY;
    }

    public void setWorldRectSouthEastY(int worldRectSouthEastY) {
        this.worldRectSouthEastY = worldRectSouthEastY;
    }

    public void setWorldRectCenterY(int worldRectCenterY) {
        this.worldRectCenterY = worldRectCenterY;
    }

    public boolean isNorthWestEdge(int x, int z) {
        return x == worldRectNorthWestX && z == worldRectNorthWestZ;
    }

    public boolean isSouthEastEdge(int x, int z) {
        return x == worldRectSouthEastX && z == worldRectSouthEastZ;
    }

    public boolean isCenter(int x, int z) {
        return x == worldRectCenterX && z == worldRectCenterZ;
    }
}
