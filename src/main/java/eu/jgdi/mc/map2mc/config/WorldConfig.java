package eu.jgdi.mc.map2mc.config;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import eu.jgdi.mc.map2mc.config.csv.BiomesCsvContent;
import eu.jgdi.mc.map2mc.config.csv.SurfaceCsvContent;
import eu.jgdi.mc.map2mc.config.csv.TerrainCsvContent;
import eu.jgdi.mc.map2mc.model.minecraft.Block;
import eu.jgdi.mc.map2mc.utils.Logger;
import eu.jgdi.mc.map2mc.utils.SortedProperties;

public class WorldConfig {

    private static final Logger logger = Logger.logger();

    public class NaturalResource {

        private Block block;

        private double percent;

        private double percentFactor;

        private int requiredTopLevel;

        private int minLevel;

        private int maxLevel;

        public NaturalResource(Block block, double percent, int requiredTopLevel, int minLevel, int maxLevel) {
            this.block = block;
            this.percent = percent;
            this.percentFactor = percent / 100d;
            this.requiredTopLevel = requiredTopLevel;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        public Block getBlock() {
            return block;
        }

        public double getPercent() {
            return percent;
        }

        public int getRequiredTopLevel() {
            return requiredTopLevel;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public double getPercentFactor() {
            return percentFactor;
        }

        public long getMaxStack() {
            return maxLevel - minLevel;
        }

        public long calculateCurrentMaxStack(long baseTopLevel, long maxTopLevel) {
            return Math.min(maxLevel, maxTopLevel) - Math.max(minLevel, baseTopLevel) + 1;
        }
    }

    private List<NaturalResource> naturalResources;

    private String configPath;

    private String fileTerrainImage;

    private String fileSurfaceImage;

    private String fileMountainsImage;

    private String fileBiomesImage;

    private String fileTerrainCsv;

    private String fileSurfaceCsv;

    private String fileBiomesCsv;

    private String dirOutputTmp;

    private String dirOutputRegion;

    private TerrainCsvContent terrainCsvContent;

    private SurfaceCsvContent surfaceCsvContent;

    private BiomesCsvContent biomesCsvContent;

    private final int mountainsLevelStart;

    private final int waterLevel;

    private final int baseLevel;

    private final int threadCount;

    private Rectangle area;

    private int originX;

    private int originY;

    private boolean clearRegion;

    public WorldConfig(File configFilePath, boolean initializeOnly) throws IOException {
        boolean configFileExists = false;
        SortedProperties properties = new SortedProperties();
        try {
            this.configPath = configFilePath.getParent();
            properties.load(new FileInputStream(configFilePath));
            configFileExists = true;
        } catch (IOException ex) {
            logger.info("Config file does not exist. Creating one ...", configFilePath);
        }

        this.naturalResources = new ArrayList<>();
        this.baseLevel = readInteger(properties, "level.base", 40);
        this.waterLevel = readInteger(properties, "level.water", 20);
        this.mountainsLevelStart = readInteger(properties, "level.mountains-start", 0);
        this.threadCount = readInteger(properties, "option.threadCount", 4);

        // files
        this.fileTerrainImage = readString(properties, "file.terrain.image", "./terrain.bmp");
        this.fileTerrainCsv = readString(properties, "file.terrain.csv", "./terrain.csv");
        this.fileSurfaceImage = readString(properties, "file.surface.image", "./terrain.bmp");
        this.fileSurfaceCsv = readString(properties, "file.surface.csv", "./surface.csv");
        this.fileBiomesCsv = readString(properties, "file.biomes.csv", "./biomes.csv");
        this.fileMountainsImage = readString(properties, "file.mountains.image", null);
        this.fileBiomesImage = readString(properties, "file.biomes.image", null);
        this.dirOutputTmp = readString(properties, "directory.output.tmp", "./tmp");
        this.dirOutputRegion = readString(properties, "directory.output.region", "./region");
        this.clearRegion = readBoolean(properties, "option.region.clear", true);
        addNaturalResource(properties, "emerald", Block.EMERALD_ORE);
        addNaturalResource(properties, "diamond", Block.DIAMOND_ORE);
        addNaturalResource(properties, "gold", Block.GOLD_ORE);
        addNaturalResource(properties, "iron", Block.IRON_ORE);
        addNaturalResource(properties, "coal", Block.COAL_ORE);

        // origin position = point (0,0) in the Minecraft world
        int[] originPoint = readPoint(properties, "origin", "(0,0)");
        this.originX = originPoint[0];
        this.originY = originPoint[1];

        // Rectangle
        area = readArea(properties);

        if (!configFileExists || initializeOnly) {
            FileOutputStream outStream = new FileOutputStream(configFilePath);
            properties.store(outStream, "Automatically created config file");
            logger.info("Configuration file created: {0}", configFilePath.getAbsolutePath());
        }

        init(initializeOnly);
    }

    private int[] readPoint(SortedProperties properties, String name, String defaultValue) {
        String pointStr = readString(properties, name, defaultValue);
        return parsePoint(pointStr);
    }

    private Rectangle readArea(Properties properties) {
        String area = readString(properties, "area", "");
        if (area == null || area.isBlank()) {
            return null;
        }
        String[] points = area.split("-");
        if (points.length != 2) {
            logger.error("Wrong area format. Expected: '(x1,y2)-(x2,y2)' but is: '" + area + "'");
        }
        int[] p1 = parsePoint(points[0]);
        int[] p2 = parsePoint(points[1]);
        validateCoordinateSystemValue("area (x1)", p1[0]);
        validateCoordinateSystemValue("area (y1)", p1[1]);
        validateCoordinateSystemValue("area (x2)", p2[0]);
        validateCoordinateSystemValue("area (y2)", p2[1]);
        return new Rectangle(
                Math.min(p1[0], p2[0]),
                Math.min(p1[1], p2[1]),
                Math.abs(p2[0] - p1[0]),
                Math.abs(p2[1] - p1[1]));
    }

    private void addNaturalResource(Properties properties, String name, Block block) {
        int requiredTopLevel = readInteger(properties, "element." + name + ".requiredTopLevel", 255);
        String levels = readString(properties, "element." + name + ".levels", "0");
        String[] levelParts = levels.split("-");
        int minLevel = Integer.parseInt(levelParts[0]);
        int maxLevel = minLevel;
        if (levelParts.length == 2) {
            maxLevel = Integer.parseInt(levelParts[1]);
        }
        double percentage = readDouble(properties, "element." + name + ".percent", 0);
        if (requiredTopLevel < 255 && maxLevel > 0 && percentage > 0) {
            naturalResources.add(new NaturalResource(block, percentage, requiredTopLevel, minLevel, maxLevel));
        }
    }

    private void init(boolean initializeOnly) {
        try {
            File file = buildFile(dirOutputRegion);
            if (file.exists()) {
                if (isClearRegion()) {
                    FileUtils.deleteDirectory(file);
                    file.mkdirs();
                }
            } else {
                file.mkdirs();
            }

            file = buildFile(dirOutputTmp);
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
            file.mkdirs();

            file = buildFile(fileTerrainCsv);
            if (!file.exists()) {
                logger.info("Terrain CSV does not exist. Creating one ...");
                terrainCsvContent = TerrainCsvContent.createNew(file);
                logger.info("Terrain CSV created: {0}", file.getAbsolutePath());
            } else {
                terrainCsvContent = TerrainCsvContent.loadExisting(file);
                logger.info("Terrain CSV loaded: {0}", file.getAbsolutePath());
            }
            file = buildFile(fileSurfaceCsv);
            if (!file.exists()) {
                logger.info("Surface CSV does not exist. Creating one ...");
                surfaceCsvContent = SurfaceCsvContent.createNew(file);
                logger.info("Surface CSV created: {0}", file.getAbsolutePath());
            } else {
                surfaceCsvContent = SurfaceCsvContent.loadExisting(file);
                logger.info("Surface CSV loaded: {0}", file.getAbsolutePath());
            }
            file = buildFile(fileBiomesCsv);
            if (!file.exists()) {
                logger.info("Biomes CSV does not exist. Creating one ...");
                biomesCsvContent = BiomesCsvContent.createNew(file);
                logger.info("Biomes CSV created: {0}", file.getAbsolutePath());
            } else {
                biomesCsvContent = BiomesCsvContent.loadExisting(file);
                logger.info("Biomes CSV loaded: {0}", file.getAbsolutePath());
            }
            file = buildFile(fileTerrainImage);
            if (!file.exists()) {
                if (initializeOnly) {
                    logger.info("Terrain image file does not exist: {0}", file.getAbsolutePath());
                } else {
                    logger.error("Terrain image file does not exist: {0}", file.getAbsolutePath());
                    System.exit(1);
                }
            }
            file = buildFile(fileSurfaceImage);
            if (!file.exists()) {
                if (initializeOnly) {
                    logger.info("Surface image file does not exist: {0}", file.getAbsolutePath());
                } else {
                    logger.error("Surface image file does not exist: {0}", file.getAbsolutePath());
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize", e);
        }
    }

    private File buildFile(String path) {
        if (configPath != null && path.startsWith("./")) {
            return Path.of(configPath, path.substring(2)).toFile();
        } else {
            String filePath = path;
            if (filePath.startsWith("~")) {
                filePath = System.getProperty("user.home") + filePath.substring(1);
            }
            return new File(filePath);
        }
    }

    public String getTerrainImagePath() {
        return fileTerrainImage;
    }

    public String getSurfaceImagePath() {
        return fileSurfaceImage;
    }

    public String getMountainsImagePath() {
        return fileMountainsImage;
    }

    public File getMountainsImageFile() {
        return fileMountainsImage != null ? buildFile(fileMountainsImage) : null;
    }

    public String getBiomesImagePath() {
        return fileBiomesImage;
    }

    public File getBiomesImageFile() {
        return fileBiomesImage != null ? buildFile(fileBiomesImage) : null;
    }

    public File getTerrainImageFile() {
        return buildFile(fileTerrainImage);
    }

    public File getSurfaceImageFile() {
        return buildFile(fileSurfaceImage);
    }

    public boolean areTerrainAndSurfaceFileTheSame() {
        return fileSurfaceImage.equals(fileTerrainImage);
    }

    public File getOutputTmpDirectory() {
        return buildFile(dirOutputTmp);
    }

    public File getOutputRegionDirectory() {
        return buildFile(dirOutputRegion);
    }

    public TerrainCsvContent getTerrainCsvContent() {
        return terrainCsvContent;
    }

    public SurfaceCsvContent getSurfaceCsvContent() {
        return surfaceCsvContent;
    }

    public BiomesCsvContent getBiomesCsvContent() {
        return biomesCsvContent;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public int getBaseLevel() {
        return baseLevel;
    }

    public int getMountainsLevelStart() {
        return mountainsLevelStart;
    }

    private String readString(Properties properties, String name, String defaultValue) {
        String value = properties.getProperty(name, null);
        if (value == null) {
            if (defaultValue != null) {
                properties.setProperty(name, defaultValue);
            }
            value = defaultValue;
        }
        logger.info("- {0} = {1}", name, value);
        return value;
    }

    private boolean readBoolean(Properties properties, String name, boolean defaultValue) {
        String value = properties.getProperty(name, null);
        if (value == null) {
            properties.setProperty(name, String.valueOf(defaultValue));
            value = String.valueOf(defaultValue);
        }
        logger.info("- {0} = {1}", name, value);
        return Boolean.parseBoolean(value);
    }

    private int readAndValidateCoordinateSystemValue(Properties properties, String name, long defaultValue) {
        String strValue = properties.getProperty(name, null);
        if (strValue == null) {
            properties.setProperty(name, String.valueOf(defaultValue));
            strValue = String.valueOf(defaultValue);
        }
        logger.info("- {0} = {1}", name, strValue);
        int value = Integer.parseInt(strValue);
        validateCoordinateSystemValue(name, value);
        return value;
    }

    private void validateCoordinateSystemValue(String name, long value) {
        if (value % 512 != 0) {
            throw new IllegalArgumentException("Value of '" + name + "' must be multiple of 512. " + value + " % 512 = " + (value % 512));
        }
    }

    private int readInteger(Properties properties, String name, long defaultValue) {
        String strValue = properties.getProperty(name, null);
        if (strValue == null) {
            properties.setProperty(name, String.valueOf(defaultValue));
            strValue = String.valueOf(defaultValue);
        }
        logger.info("- {0} = {1}", name, strValue);
        return Integer.parseInt(strValue);
    }

    private double readDouble(Properties properties, String name, double defaultValue) {
        String value = properties.getProperty(name, null);
        if (value == null) {
            properties.setProperty(name, String.valueOf(defaultValue));
            value = String.valueOf(defaultValue);
        }
        logger.info("- {0} = {1}", name, value);
        return Double.parseDouble(value);
    }

    public int getThreadCount() {
        return threadCount;
    }

    public Rectangle getArea() {
        return area;
    }

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }

    public boolean isClearRegion() {
        return clearRegion;
    }

    public List<NaturalResource> getNaturalResources() {
        return naturalResources;
    }

    private int[] parsePoint(String point) {
        if (point.startsWith("(") && point.endsWith(")")) {
            String[] str = point.substring(1, point.length() - 1).split(",");
            if (str.length != 2) {
                throw new IllegalArgumentException("Point '" + point + "' with unexpected format. Expecting '(x,y)'");
            }
            int[] result = new int[2];
            result[0] = Integer.parseInt(str[0]);
            result[1] = Integer.parseInt(str[1]);
            return result;
        } else {
            throw new IllegalArgumentException("Point '" + point + "' with unexpected format. Expecting '(x,y)'");
        }
    }
}
