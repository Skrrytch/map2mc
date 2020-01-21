package eu.jgdi.mc.map2mc.renderer;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.RegionLocation;
import eu.jgdi.mc.map2mc.model.raw.RegionInfoMap;

public class AnvilRenderer extends Renderer {

    private final WorldRepository worldRepo;

    private final WorldConfig config;

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
        List<AnvilRegionRendererThread> threads = new ArrayList<>();
        for (File file : files) {
            try {
                fileCount++;
                // logger.info("Rending file {0} of {1} (''{2}'') ...", fileCount, files.size(), file.getAbsolutePath());
                RegionInfoMap region = new RegionInfoMap(regionLocationMap.get(file), Files.readAllBytes(file.toPath()));

                // renderRegion(region, properties, outputDir);
                AnvilRegionRendererThread anvilRegionRendererThread =
                        new AnvilRegionRendererThread(fileCount, file.getName(), latch, region, worldRepo);
                threads.add(anvilRegionRendererThread);
                executor.execute(anvilRegionRendererThread);
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
            List<String> messageList = threads.stream().flatMap(thread -> thread.getMessages().stream())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            if (messageList.size() > 0) {
                logger.warn("Validation info: {0} message(s) from the region renderers", messageList.size());
                for (String message : messageList) {
                    logger.warn("  {0}", message);
                }
            } else {
                logger.info("Validation info: No messages from region renderers");
            }
        } catch (InterruptedException e) {
            logger.error(e, "Failure while waiting for AnvilRenderer threads: {0}", e.getLocalizedMessage());
        }
    }
}
