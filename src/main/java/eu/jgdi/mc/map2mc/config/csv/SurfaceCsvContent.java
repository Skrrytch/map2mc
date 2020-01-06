package eu.jgdi.mc.map2mc.config.csv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import eu.jgdi.mc.map2mc.utils.Logger;

public class SurfaceCsvContent extends AbstractCsvContent {

    private final static Logger logger = Logger.logger();

    private final static String HEADER_INDEX = "ColorIndex";

    private final static String HEADER_BLOCK = "Block";

    private final static String HEADER_DEPTH = "Depth";

    private final static String HEADER_DESCR = "Description";

    private CSVFormat csvFormat;

    private Map<Integer, Record> map;

    public static class Record {

        private int colorIndex;

        private String blockId;

        private String additionalBlockId;

        private float additionalBlockFrequency;

        private byte depth;

        private String description;

        public Record(int colorIndex, String blockIdDef, byte depth, String description) {
            this.colorIndex = colorIndex;
            List<String> list = Arrays.stream(blockIdDef.split("\\+")).collect(Collectors.toList());
            this.blockId = list.get(0);
            this.additionalBlockFrequency = 0f;
            this.additionalBlockId = null;
            if (list.size() >= 2) {
                this.additionalBlockId = splitBlockId(list.get(1));
                this.additionalBlockFrequency = splitFrequency(list.get(1));
            }
            this.depth = depth;
            this.description = description;
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

        public String getBlockId() {
            return blockId;
        }

        public String getAdditionalBlockId() {
            return additionalBlockId;
        }

        public boolean hasAdditionalBlock() {
            return additionalBlockId != null;
        }

        public float getAdditionalBlockFrequency() {
            return additionalBlockFrequency;
        }

        public byte getDepth() {
            return depth;
        }

        public String getDescription() {
            return description;
        }
    }

    public SurfaceCsvContent() {
        this.csvFormat = CSVFormat.EXCEL.withHeader();
        this.map = new LinkedHashMap<>();
    }

    public Record getByColorIndex(int surfaceColorIndex) {
        return map.get(surfaceColorIndex);
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

    private CSVParser load(File file) {
        try {
            CSVParser parser = csvFormat.parse(new FileReader(file));
            for (CSVRecord csvRecord : parser.getRecords()) {
                byte colorIndexValue = readByte(csvRecord, HEADER_INDEX);
                String blockId = csvRecord.get(HEADER_BLOCK);
                byte depth = readByte(csvRecord, HEADER_DEPTH, (byte) 1);
                String description = csvRecord.get(HEADER_DESCR);
                Record record = new Record(
                        colorIndexValue,
                        blockId.trim(),
                        depth,
                        description);
                map.put(record.getColorIndex(), record);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read from file '" + file.getAbsolutePath() + "'", e);
        }
        return null;
    }

    private byte readByte(CSVRecord csvRecord, String name) {
        try {
            String value = csvRecord.get(name);
            if (value == null || value.trim().isEmpty()) {
                logger.error("Failed to read {0}: Value required!", name);
                System.exit(1);
            }
            return Byte.parseByte(value.trim());
        } catch (RuntimeException ex) {
            String colorIndexValue = csvRecord.get(HEADER_INDEX);
            logger.error("Failed to read {0} from record.", name, colorIndexValue);
            System.exit(1);
            return 0;
        }
    }

    private byte readByte(CSVRecord csvRecord, String name, byte defaultValue) {
        try {
            String value = csvRecord.get(name);
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Byte.parseByte(value.trim());
        } catch (RuntimeException ex) {
            String colorIndexValue = csvRecord.get(HEADER_INDEX);
            logger.error("Failed to read {0} from record with index {1}", name, colorIndexValue);
            System.exit(1);
            return defaultValue;
        }
    }

    private void store(File file) {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(file), CSVFormat.EXCEL)) {
            printer.printRecord(HEADER_INDEX, HEADER_BLOCK, HEADER_DEPTH, HEADER_DESCR);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to write CSV", ex);
        }
    }
}
