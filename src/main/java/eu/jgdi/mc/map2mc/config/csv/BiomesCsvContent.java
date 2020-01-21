package eu.jgdi.mc.map2mc.config.csv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class BiomesCsvContent extends AbstractCsvContent {

    private final static String HEADER_INDEX = "ColorIndex";

    private final static String HEADER_BIOMEID = "BiomeId";

    private final static String HEADER_BIOMENAME = "BiomeName";

    private final static String HEADER_DESCR = "Description";

    private final CSVFormat csvFormat;

    private final Map<Integer, Record> map = new LinkedHashMap<>();

    public Record getByColorIndex(int terrainColorIndex) {
        return map.get(terrainColorIndex);
    }

    public static class Record {

        private final int colorIndex;

        private final int biomeId;

        private final String biomeName;

        private final String description;

        public Record(int colorIndex, int biomeId, String biomeName, String description) {
            this.colorIndex = colorIndex;
            this.biomeId = biomeId;
            this.biomeName = biomeName;
            this.description = description;
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public int getBiomeId() {
            return biomeId;
        }

        public String getBiomeName() {
            return biomeName;
        }

        public String getDescription() {
            return description;
        }
    }

    public BiomesCsvContent() {
        this.csvFormat = CSVFormat.EXCEL.withHeader();
    }

    public static BiomesCsvContent createNew(File file) {
        BiomesCsvContent result = new BiomesCsvContent();
        result.storeEmptyFile(file);
        return result;
    }

    public static BiomesCsvContent loadExisting(File file) {
        BiomesCsvContent result = new BiomesCsvContent();
        result.load(file);
        return result;
    }

    private void load(File file) {
        try {
            CSVParser parser = csvFormat.parse(new FileReader(file));
            for (CSVRecord csvRecord : parser.getRecords()) {
                String colorIndexValue = csvRecord.get(HEADER_INDEX);
                String biomeId = csvRecord.get(HEADER_BIOMEID);
                String biomeName = csvRecord.get(HEADER_BIOMENAME);
                String description = csvRecord.get(HEADER_DESCR);
                Record record = new Record(
                        Integer.parseInt(colorIndexValue.trim()),
                        Integer.parseInt(biomeId.trim()),
                        biomeName.trim(),
                        description);
                map.put(record.getColorIndex(), record);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read from file '" + file.getAbsolutePath() + "'", e);
        }
    }

    private void storeEmptyFile(File file) {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(file), CSVFormat.EXCEL)) {
            printer.printComment(
                    "Terrain definition: Use the color indexes of the terrain image and define their level" +
                            "(relative to the sea level");
            printer.printRecord(HEADER_INDEX, HEADER_BIOMEID, HEADER_BIOMENAME, HEADER_DESCR);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to write CSV", ex);
        }
    }
}
