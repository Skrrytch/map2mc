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

public class TerrainCsvContent extends AbstractCsvContent {

    private final static String HEADER_INDEX = "ColorIndex";

    private final static String HEADER_LEVEL = "Level";

    private final static String HEADER_RELATIVE_TO = "RelativeTo";

    private final static String HEADER_DESCR = "Description";

    private CSVFormat csvFormat;

    private Map<Integer, Record> map = new LinkedHashMap<>();

    public Record getByColorIndex(int terrainColorIndex) {
        return map.get(terrainColorIndex);
    }

    public static class Record {

        private int colorIndex;

        private int level;

        private final Relation relativeTo;

        private String description;

        private enum Relation {
            SEA,
            NEIGHBORS;

            public static Relation of(String relativeToValue) {
                switch (relativeToValue.toUpperCase()) {
                    case "NEIGHBORS":
                        return NEIGHBORS;
                    case "SEA":
                    default:
                        return SEA;
                }
            }
        }

        public Record(int colorIndex, int level, Relation relation, String description) {
            this.colorIndex = colorIndex;
            this.level = level;
            this.relativeTo = relation;
            this.description = description;
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public int getLevel() {
            return level;
        }

        public Relation getRelativeTo() {
            return relativeTo;
        }

        public String getDescription() {
            return description;
        }
    }

    public TerrainCsvContent() {
        this.csvFormat = CSVFormat.EXCEL.withHeader();
    }

    public static TerrainCsvContent createNew(File file) {
        TerrainCsvContent result = new TerrainCsvContent();
        result.storeEmptyFile(file);
        return result;
    }

    public static TerrainCsvContent loadExisting(File file) {
        TerrainCsvContent result = new TerrainCsvContent();
        result.load(file);
        return result;
    }

    private CSVParser load(File file) {
        try {
            CSVParser parser = csvFormat.parse(new FileReader(file));
            for (CSVRecord csvRecord : parser.getRecords()) {
                String colorIndexValue = csvRecord.get(HEADER_INDEX);
                String levelValue = csvRecord.get(HEADER_LEVEL);
                String relativeToValue = csvRecord.get(HEADER_RELATIVE_TO);
                String description = csvRecord.get(HEADER_DESCR);
                Record record = new Record(
                        Integer.parseInt(colorIndexValue.trim()),
                        Integer.parseInt(levelValue.trim()),
                        Record.Relation.of(relativeToValue),
                        description);
                map.put(record.getColorIndex(), record);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read from file '" + file.getAbsolutePath() + "'", e);
        }
        return null;
    }

    private void storeEmptyFile(File file) {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(file), CSVFormat.EXCEL)) {
            printer.printComment(
                    "Terrain definition: Use the color indexes of the terrain image and define their level" +
                            "(relative to the sea level");
            printer.printRecord(HEADER_INDEX, HEADER_LEVEL, HEADER_RELATIVE_TO, HEADER_DESCR);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to write CSV", ex);
        }
    }
}
