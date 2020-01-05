package eu.jgdi.mc.map2mc;

import java.io.File;
import java.util.Arrays;

import eu.jgdi.mc.map2mc.utils.Logger;

public class Main {

    private static final String ARG_DIRECTORY = "-dir";

    private static Logger logger = Logger.logger();

    public static void main(String[] args) {

        if (areArgumentCorrect(args)) {
            displayHelpAndExit();
        }

        File directory = getDirectoryFromArgs(args);
        boolean initializeOnly = !directory.exists();
        if (initializeOnly) {
            logger.info("Directory does not exist: Initializing ....");
        }

        MapToMinecraftConverter converter = MapToMinecraftConverter.init(directory, initializeOnly);
        if (initializeOnly) {
            displayInstructionsAndExit(directory);
        }

        converter.parseWorld();
        converter.renderWorld();

        logger.info("Finished.");
    }

    private static void displayInstructionsAndExit(File directory) {
        logger.info("Directory initialized: {0}", directory.getAbsolutePath());
        logger.info("Finished with initializing.");
        logger.info("- Copy you map to {0}/terrain.bmp", directory.getAbsolutePath());
        logger.info("- Fill at least the terrain definition file: {0}/terrain.csv", directory.getAbsolutePath());
        logger.info("- Modify configuration file to your need: {0}/config.properties", directory.getAbsolutePath());
        System.exit(1);
    }

    private static boolean areArgumentCorrect(String[] args) {
        if (args.length == 0) {
            return true;
        }
        if (Arrays.stream(args).noneMatch(arg -> arg.startsWith(ARG_DIRECTORY + "="))) {
            return true;
        }
        return false;
    }

    private static void displayHelpAndExit() {
        System.out.println("Parameters");
        System.out.println("  " + ARG_DIRECTORY + "=(path to directory)");
        System.out.println();
        System.out.println("When the directory does not exists it will be created with some skeleton files");
        System.exit(0);
    }

    private static File getDirectoryFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith(ARG_DIRECTORY + "=")) {
                String directory = arg.substring(ARG_DIRECTORY.length() + 1).trim();
                logger.info("Using directory: {0}", directory);
                return new File(directory);
            }
        }
        logger.error("Missing argument ''{0}''", ARG_DIRECTORY);
        System.exit(1);
        return null;
    }
}
