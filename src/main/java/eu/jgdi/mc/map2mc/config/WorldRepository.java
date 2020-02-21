package eu.jgdi.mc.map2mc.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import net.querz.nbt.CompoundTag;

import eu.jgdi.mc.map2mc.config.csv.BlockStack;
import eu.jgdi.mc.map2mc.config.csv.CompoundDef;
import eu.jgdi.mc.map2mc.config.csv.SurfaceCsvContent;
import eu.jgdi.mc.map2mc.model.minecraft.Block;
import eu.jgdi.mc.map2mc.utils.Logger;

public class WorldRepository {

    private static final Logger logger = Logger.logger();

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

    private Map<String, CompoundDef> compoundDefMap = new HashMap<>();

    public WorldRepository(WorldConfig config) {
        this.config = config;
        this.init();
    }

    private void init() {

        Set<String> unknownBlockTypes = new HashSet<>();

        Map<Integer, SurfaceCsvContent.Record> map = config.getSurfaceCsvContent().getMap();
        int row = 0;
        for (SurfaceCsvContent.Record record : map.values()) {
            row++;
            try {
                // Surface blocks
                BlockStack surfaceStack = record.getSurfaceStack();
                List<CompoundDef> surfaceCompounds = new ArrayList<>();
                for (String blockId : surfaceStack.getBlockIdList()) {
                    if (!Block.BLOCK_INFO.containsKey(blockId)) {
                        unknownBlockTypes.add(blockId);
                    }
                    surfaceCompounds.add(this.getBlockCompoundDef(blockId));
                }
                surfaceStack.setCompoundDefList(surfaceCompounds);

                // Item blocks

                if (record.hasItems()) {
                    for (BlockStack itemStack : record.getItemStackList()) {
                        if (itemStack == null) {
                            throw new IllegalStateException("Item stack is <null>");
                        }
                        List<CompoundDef> itemCompounds = new ArrayList<>();
                        for (String blockId : itemStack.getBlockIdList()) {
                            if (!Block.BLOCK_INFO.containsKey(blockId)) {
                                unknownBlockTypes.add(blockId);
                            }
                            itemCompounds.add(this.getBlockCompoundDef(blockId));
                        }
                        itemStack.setCompoundDefList(itemCompounds);
                    }
                }
            } catch (Exception ex) {
                logger.error(ex, "Failure at surface mapping record #{0}, color index {1}", row, record.getColorIndex());
                System.exit(1);
            }
        }
        List<String> unexpectedBlocks = unknownBlockTypes.stream().distinct().sorted().collect(Collectors.toList());
        if (unexpectedBlocks.size() == 0) {
            logger.info("All block types in surface csv are well known.");
        } else {
            logger.warn("{0} unexpected block types in suerface csv, please review!", unexpectedBlocks.size());
            for (String unexpectedBlock : unexpectedBlocks) {
                logger.warn("  {0}", unexpectedBlock);
            }
            System.out.println();
            System.out.print("Please confirm that you want to proceed (type 'yes') ... ");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();
            if (!answer.equalsIgnoreCase("yes")) {
                System.exit(0);
            }
            System.out.println();
        }
    }

    public WorldConfig getConfig() {
        return config;
    }

    public CompoundDef getBlockCompoundDef(String id) {
        if (!compoundDefMap.containsKey(id)) {
            CompoundTag compoundTag = new CompoundTag();
            CompoundDef def = new CompoundDef(id, compoundTag);
            if (id.equalsIgnoreCase(Block.UNDEFINED)) {
                def.setUndefined(true);
                // When you see gold ore on the surface, you have used colors that are mapped to "--UNDEFINED--"
                compoundTag.putString("Name", "minecraft:" + Block.GOLD_ORE.getBlockId());
            } else if (id.equalsIgnoreCase(Block.AIR)) {
                def.setAir(true);
                // When you see emerald ore on the surface, a bug exists that renderes air as a valid block
                compoundTag.putString("Name", "minecraft:" + Block.EMERALD_ORE.getBlockId());
            } else {
                compoundTag.putString("Name", "minecraft:" + id);
            }
            compoundDefMap.put(id, def);
        }
        return compoundDefMap.get(id);
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
