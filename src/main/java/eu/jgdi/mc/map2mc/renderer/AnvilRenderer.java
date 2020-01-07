package eu.jgdi.mc.map2mc.renderer;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.RegionLocation;
import eu.jgdi.mc.map2mc.model.raw.RegionInfoMap;
import eu.jgdi.mc.map2mc.utils.Logger;

public class AnvilRenderer extends Renderer {

    private WorldRepository worldRepo;

    private WorldConfig config;

    public AnvilRenderer(WorldRepository worldRepo) {
        this.worldRepo = worldRepo;
        this.config = worldRepo.getConfig();
    }

    @Override
    public void render() {

        File[] filesInDirectory = config.getOutputTmpDirectory().listFiles();
        if (filesInDirectory == null) {
            throw new IllegalStateException("Temporary directory is empty: No intermediate files found.");
        }
        Map<File, RegionLocation> regionLocationMap = mapToRegions(Arrays.asList(filesInDirectory));

        Set<File> files = regionLocationMap.keySet();
        long fileCount = 0;

        logger.info("Processing {0} files, using {1} thread(s) ...", files.size(), config.getThreadCount());
        ExecutorService executor = Executors.newFixedThreadPool(config.getThreadCount());
        CountDownLatch latch = new CountDownLatch(files.size());
        for (File file : files) {
            try {
                fileCount++;
                // logger.info("Rending file {0} of {1} (''{2}'') ...", fileCount, files.size(), file.getAbsolutePath());
                RegionInfoMap region = new RegionInfoMap(
                        regionLocationMap.get(file),
                        Files.readAllBytes(file.toPath()));

                // renderRegion(region, properties, outputDir);
                executor.execute(new AnvilRegionRendererThread(file.getName(), latch, region, worldRepo));
            } catch (Exception e) {
                logger.error(e, "Failure while creating for AnvilRenderer thread #{0}: {1}", fileCount, e.getLocalizedMessage());
            }
        }
        try {

            if (latch.await(4, TimeUnit.HOURS)) {
                logger.info("Anvil rendering threads all finished.");
                executor.shutdownNow();
            } else {
                logger.info("Anvil rendering threads not yet finished. Giving up!");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error(e, "Failure while waiting for AnvilRenderer threads: {0}", e.getLocalizedMessage());
        }
    }
}
