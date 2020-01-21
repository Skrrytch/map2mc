package eu.jgdi.mc.map2mc.config.csv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import eu.jgdi.mc.map2mc.model.minecraft.Block;
import eu.jgdi.mc.map2mc.utils.Logger;

public class SurfaceCsvContent extends AbstractCsvContent {

    private final static Logger logger = Logger.logger();

    private final static String HEADER_INDEX = "ColorIndex";

    private final static String HEADER_BIOMENAME = "BiomeName";

    private final static String HEADER_BLOCK = "Block";

    private final static String HEADER_ADDITIONAL_BLOCK = "AdditionalBlocks";

    private final static String HEADER_DEPTH = "Depth";

    private final static String HEADER_DESCR = "Description";

    private CSVFormat csvFormat;

    private Map<String, Record> map;

    public static class AdditionalItem {

        String blockId;

        float frequency;

        public AdditionalItem(String blockId, float frequency) {
            this.blockId = blockId;
            this.frequency = frequency;
        }

        public String getBlockId() {
            return blockId;
        }

        public float getFrequency() {
            return frequency;
        }
    }

    public static class Record {

        private int colorIndex;

        private List<String> blockIds;

        private String biomeName;

        private List<AdditionalItem> additionalBlocks;

        private int depth;

        private String description;

        public Record(
                int colorIndex,
                String blockIdDef,
                String biomeName,
                String additionalBlockIdDef,
                int depth,
                String description) {
            this.colorIndex = colorIndex;
            this.blockIds = Arrays.stream(blockIdDef.split("\\+")).collect(Collectors.toList());
            this.biomeName = biomeName;
            this.additionalBlocks = additionalBlockIdDef.isEmpty() ? null : parseAdditionalBlock(additionalBlockIdDef);
            this.depth = depth;
            this.description = description;
        }

        private List<AdditionalItem> parseAdditionalBlock(String additionalBlockIdDef) {
            List<AdditionalItem> result = new ArrayList<>();
            List<String> blockDefList = Arrays.stream(additionalBlockIdDef.split("/")).collect(Collectors.toList());
            for (String blockDef : blockDefList) {
                String blockId = splitBlockId(blockDef);
                float frequency = splitFrequency(blockDef);
                result.add(new AdditionalItem(blockId, frequency));
            }
            return result;
        }

        private float splitFrequency(String id) {
            String[] parts = id.split(":");
            if (parts.length < 2) {
                return 1f;
            }
            return Integer.parseInt(parts[1]) / 100f;
        }

        private String splitBlockId(String id) {
            return id.split(":")[0];
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public List<String> getBlockIds() {
            return blockIds;
        }

        public List<AdditionalItem> getAdditionalBlocks() {
            return additionalBlocks;
        }

        public boolean hasAdditionalBlock() {
            return additionalBlocks != null && additionalBlocks.size() > 0;
        }

        public int getDepth() {
            return depth;
        }

        public String getBiomeName() {
            return biomeName;
        }

        public String getDescription() {
            return description;
        }
    }

    public SurfaceCsvContent() {
        this.csvFormat = CSVFormat.EXCEL.withHeader();
        this.map = new LinkedHashMap<>();
    }

    public Record getByColorIndex(int surfaceColorIndex, String biomeName) {
        Record record = null;
        if (biomeName != null && !biomeName.isBlank()) {
            record = map.get(surfaceColorIndex + "/" + biomeName);
        }
        if (record == null) {
            record = map.get(String.valueOf(surfaceColorIndex));
        }
        return record;
    }

    public static SurfaceCsvContent createNew(File file) {
        SurfaceCsvContent result = new SurfaceCsvContent();
        result.store(file);
        return result;
    }

    public static SurfaceCsvContent loadExisting(File file) {
        SurfaceCsvContent result = new SurfaceCsvContent();
        result.load(file);
        return result;
    }

    private void load(File file) {
        Set<String> unknownBlockTypes = new HashSet<>();
        int row = 1;
        try {
            CSVParser parser = csvFormat.parse(new FileReader(file));
            for (CSVRecord csvRecord : parser.getRecords()) {
                int colorIndexValue = readInt(csvRecord, HEADER_INDEX);
                String blockId = csvRecord.get(HEADER_BLOCK);
                String biomeName = csvRecord.get(HEADER_BIOMENAME);
                int depth = readInt(csvRecord, HEADER_DEPTH, (byte) 1);
                String additionalBlock = csvRecord.get(HEADER_ADDITIONAL_BLOCK);
                String description = csvRecord.get(HEADER_DESCR);
                Record record = new Record(
                        colorIndexValue,
                        blockId.trim(),
                        biomeName.trim(),
                        additionalBlock.trim(),
                        depth,
                        description);
                String key = biomeName.isEmpty() ? String.valueOf(colorIndexValue) : record.getColorIndex() + "/" + biomeName;
                map.put(key, record);

                validateBlocksTypes(unknownBlockTypes, record);

                row++;
            }
        } catch (RuntimeException | IOException e) {
            throw new IllegalArgumentException("Failed to read from file '" + file.getAbsolutePath() + "' at row " + row, e);
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

    private void validateBlocksTypes(Set<String> unknownBlockTypes, Record record) {
        List<String> blockIds = record.getBlockIds();
        for (String blockId : blockIds) {
            if (!Block.EXPECTED_BLOCK_TYPES.containsKey(blockId)) {
                unknownBlockTypes.add(blockId);
            }
        }
        if (record.hasAdditionalBlock()) {
            record.getAdditionalBlocks().stream()
                    .filter(item -> !Block.EXPECTED_BLOCK_TYPES.containsKey(item.getBlockId()))
                    .forEach(item -> unknownBlockTypes.add(item.getBlockId()));
        }
    }

    private int readInt(CSVRecord csvRecord, String name) {
        try {
            String value = csvRecord.get(name);
            if (value == null || value.trim().isEmpty()) {
                logger.error("Failed to read {0}: Value required!", name);
                System.exit(1);
            }
            return Integer.parseInt(value.trim());
        } catch (RuntimeException ex) {
            String colorIndexValue = csvRecord.get(HEADER_INDEX);
            logger.error(ex, "Failed to read {0} from record.", name, colorIndexValue);
            throw ex;
        }
    }

    private int readInt(CSVRecord csvRecord, String name, byte defaultValue) {
        try {
            String value = csvRecord.get(name);
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (RuntimeException ex) {
            String colorIndexValue = csvRecord.get(HEADER_INDEX);
            logger.error("Failed to read {0} from record with index {1}", name, colorIndexValue);
            System.exit(1);
            return defaultValue;
        }
    }

    private void store(File file) {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(file), CSVFormat.EXCEL)) {
            printer.printRecord(HEADER_INDEX, HEADER_BIOMENAME, HEADER_BLOCK, HEADER_DEPTH, HEADER_DESCR);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to write CSV", ex);
        }
    }
}
