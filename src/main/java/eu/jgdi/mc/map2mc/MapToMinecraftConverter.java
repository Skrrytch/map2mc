
package eu.jgdi.mc.map2mc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.parser.Parser;
import eu.jgdi.mc.map2mc.renderer.AnvilRenderer;
import eu.jgdi.mc.map2mc.utils.Logger;

public class MapToMinecraftConverter {

    private static Logger logger = Logger.logger();

    private WorldRepository worldRepo;

    private MapToMinecraftConverter(WorldRepository worldRepo) {
        this.worldRepo = worldRepo;
    }

    public static MapToMinecraftConverter init(File directoryFile, boolean initializeOnly) {
        logger.info("Initializing ''{0}''", directoryFile);
        WorldRepository worldRepo = initializeWorld(directoryFile, initializeOnly);
        logger.info("Initialization done.");
        return new MapToMinecraftConverter(worldRepo);
    }

    public void parseWorld() {
        logger.info("Parsing world to intermediate output ''{0}''...", worldRepo.getConfig().getOutputTmpDirectory());
        Parser parser = new Parser(worldRepo);
        parser.parseWorld();
        logger.info("Parsing done");
    }

    public void renderWorld() {
        logger.info("Rendering minecraft world to {0} ...", worldRepo.getConfig().getOutputRegionDirectory());
        AnvilRenderer anvilRenderer = new AnvilRenderer(worldRepo);
        anvilRenderer.render();
        logger.info("Rendering minecraft world done.");
    }

    private static WorldRepository initializeWorld(File directory, boolean generateConfigOnly) {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.error("Failed to create directory {0}", directory.getAbsolutePath());
                System.exit(1);
            }
        }
        File configFile = Path.of(directory.getAbsolutePath(), "config.properties").toFile();
        try {
            WorldConfig config = new WorldConfig(configFile, generateConfigOnly);
            return new WorldRepository(config);
        } catch (IOException e) {
            logger.error(e, "Failed to read from configuration file {0}", configFile.getAbsolutePath());
            System.exit(1);
            return null;
        }
    }
}
