package eu.jgdi.mc.map2mc.config;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import eu.jgdi.mc.map2mc.config.csv.BiomesCsvContent;
import eu.jgdi.mc.map2mc.config.csv.SurfaceCsvContent;
import eu.jgdi.mc.map2mc.config.csv.TerrainCsvContent;
import eu.jgdi.mc.map2mc.utils.Logger;
import eu.jgdi.mc.map2mc.utils.SortedProperties;

public class WorldConfig {

    private static final Logger logger = Logger.logger();

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

    private final int seaLevel;

    private final int baseLevel;

    private final int threadCount;

    private Rectangle rectangle;

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

        this.baseLevel = readInteger(properties, "level.base", 40);
        this.seaLevel = readInteger(properties, "level.sea", 20);
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

        // origin position = point (0,0) in the Minecraft world
        this.originX = readAndValidateCoordinateSystemValue(properties, "origin.x", 0);
        this.originY = readAndValidateCoordinateSystemValue(properties, "origin.y", 0);

        // Rectangle
        int x = readAndValidateCoordinateSystemValue(properties, "rectangle.x", 0);
        int y = readAndValidateCoordinateSystemValue(properties, "rectangle.y", 0);
        int width = readAndValidateCoordinateSystemValue(properties, "rectangle.width", 0);
        int height = readAndValidateCoordinateSystemValue(properties, "rectangle.height", 0);
        if (width > 0 && height > 0) {
            rectangle = new Rectangle(x, y, width, height);
        }

        if (!configFileExists || initializeOnly) {
            FileOutputStream outStream = new FileOutputStream(configFilePath);
            properties.store(outStream, "Automatically created config file");
            logger.info("Configuration file created: {0}", configFilePath.getAbsolutePath());
        }

        init(initializeOnly);
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
                logger.info("Surface CSV does not exist. Creating one ...");
                biomesCsvContent = BiomesCsvContent.createNew(file);
                logger.info("Surface CSV created: {0}", file.getAbsolutePath());
            } else {
                biomesCsvContent = BiomesCsvContent.loadExisting(file);
                logger.info("Surface CSV loaded: {0}", file.getAbsolutePath());
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

    public int getSeaLevel() {
        return seaLevel;
    }

    public int getBaseLevel() {
        return baseLevel;
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
        if (value % 512 != 0) {
            throw new IllegalArgumentException("Value of '" + name + "' must be multiple of 512. " + value + " % 512 = " + (value % 512));
        }
        return value;
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

    public Rectangle getRectangle() {
        return rectangle;
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
}
