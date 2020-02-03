package eu.jgdi.mc.map2mc.config.csv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import eu.jgdi.mc.map2mc.utils.Logger;

public class SurfaceCsvContent extends AbstractCsvContent {

    private final static Logger logger = Logger.logger();

    private final static String HEADER_INDEX = "ColorIndex";

    private final static String HEADER_BLOCK = "Blocks";

    private final static String HEADER_ITEMS = "Items";

    private final static String HEADER_DESCR = "Description";

    private CSVFormat csvFormat;

    private Map<Integer, Record> map;

    public static class Record {

        private int colorIndex;

        private BlockStack blockStack;

        private List<BlockStack> itemStackList;

        private String description;

        public Record(
                int colorIndex,
                String blockStackDef,
                String itemStackListDef,
                String description) {
            this.colorIndex = colorIndex;
            this.blockStack = BlockStack.parseStack(blockStackDef);
            this.itemStackList = BlockStack.parseStacksWithProbability(itemStackListDef);
            this.description = description;
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public BlockStack getSurfaceStack() {
            return blockStack;
        }

        public List<BlockStack> getItemStackList() {
            return itemStackList;
        }

        public boolean hasItems() {
            return itemStackList != null && itemStackList.size() > 0;
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

    public Map<Integer, Record> getMap() {
        return map;
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
        int row = 1;
        try {
            CSVParser parser = csvFormat.parse(new FileReader(file));
            for (CSVRecord csvRecord : parser.getRecords()) {
                int colorIndexValue = readInt(csvRecord, HEADER_INDEX);
                String blockId = csvRecord.get(HEADER_BLOCK);
                String additionalBlock = csvRecord.get(HEADER_ITEMS);
                String description = csvRecord.get(HEADER_DESCR);
                Record record = new Record(
                        colorIndexValue,
                        blockId.trim().toLowerCase(),
                        additionalBlock.trim().toLowerCase(),
                        description);
                map.put(colorIndexValue, record);

                row++;
            }
        } catch (RuntimeException | IOException e) {
            throw new IllegalArgumentException("Failed to read from file '" + file.getAbsolutePath() + "' at row " + row, e);
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
            printer.printRecord(HEADER_INDEX, HEADER_BLOCK, HEADER_ITEMS, HEADER_DESCR);
            printer.printRecord("0", "--UNDEFINED--", "", "1", "Use --UNDEFINED-- when you do not use the color yet.");
            printer.printRecord("1", "sand", "", "1", "Sand (1 blocks deep)");
            printer.printRecord("2", "sand", "", "3", "Sand (3 blocks deep)");
            printer.printRecord("10", "grass_block", "grass", "1", "Grassblock with 100% grass on it");
            printer.printRecord(
                    "16",
                    "grass_block",
                    "oak_sapling:5/fern:15",
                    "1",
                    "Grassblock with an OAK sapling (5%), otherwise Fern (15%)");
            printer.printRecord("21", "sand", "cactus:1", "5", "Sand (5 blocks deep) with 1% cactus on it");
            printer.printRecord(
                    "42",
                    "stone+granite+granite",
                    "",
                    "3",
                    "Stack of 1x stone and 2x granite (3 times deep) -> altogether 9 blocks deep");
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to write CSV", ex);
        }
    }
}
