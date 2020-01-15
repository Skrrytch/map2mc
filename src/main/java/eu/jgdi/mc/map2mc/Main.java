package eu.jgdi.mc.map2mc;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import eu.jgdi.mc.map2mc.model.minecraft.Block;
import eu.jgdi.mc.map2mc.utils.Logger;

public class Main {

    private static Logger logger = Logger.logger();

    public static void main(String[] args) {

        long startedAt = System.currentTimeMillis();
        File directory = processArguments(args);
        if (directory == null) {
            System.exit(0);
        }
        boolean initializeOnly = !directory.exists();
        if (initializeOnly) {
            logger.info("Directory does not exist: Initializing ....");
        }

        MapToMinecraftConverter converter = MapToMinecraftConverter.init(directory, initializeOnly);
        if (initializeOnly) {
            displayInstructions(directory);
            System.exit(0);
        }

        converter.parseWorld();
        String targetDirectory = converter.renderWorld();

        Duration duration = Duration.of(System.currentTimeMillis() - startedAt, ChronoUnit.MILLIS);
        logger.info("Finished with ''{0}'', Minecraft world written to {1}", directory.getName(), targetDirectory);
        logger.info("Duration: {0}", humanReadableFormat(duration));
    }

    public static String humanReadableFormat(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    private static File processArguments(String[] args) {
        boolean showHelp = true;
        File directory = null;
        for (String arg : args) {
            // User wants to
            if (arg.equalsIgnoreCase("--list-blocks")) {
                showHelp = false;
                displayKnownBlocks();
            }
            if (arg.startsWith("--dir=")) {
                showHelp = false;
                String filePath = arg.substring(arg.indexOf("=") + 1).trim();
                if (filePath.startsWith("~")) {
                    filePath = System.getProperty("user.home") + filePath.substring(1);
                }
                logger.info("Using directory: {0}", filePath);
                directory = new File(filePath);
            }
        }
        if (directory != null) {
            return directory;
        }
        if (showHelp) {
            displayHelp();
        }
        return null;
    }

    private static void displayInstructions(File directory) {
        logger.info("Directory initialized: {0}", directory.getAbsolutePath());
        logger.info("Finished with initializing.");
        logger.info("- Use images with width and height which are multiple of 512");
        logger.info("- Copy you map to {0}/terrain.bmp", directory.getAbsolutePath());
        logger.info("- Fill at least the terrain definition file: {0}/terrain.csv", directory.getAbsolutePath());
        logger.info("- Modify configuration file to your need: {0}/config.properties", directory.getAbsolutePath());
    }

    private static void displayHelp() {
        System.out.println("Parameters");
        System.out.println("  --dir=(path to directory)");
        System.out.println("  --list-blocks to list all known minecraft blocks");
        System.out.println();
        System.out.println("When the directory does not exists it will be created with some skeleton files");
    }

    private static void displayKnownBlocks() {
        System.out.println("List of known blocks (there are many other that are also supported)");
        Block.EXPECTED_BLOCK_TYPES.stream()
                .sorted().forEach(blockId -> System.out.println("  " + blockId));
    }
}
