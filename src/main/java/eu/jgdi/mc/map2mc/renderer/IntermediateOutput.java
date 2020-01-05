package eu.jgdi.mc.map2mc.renderer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import eu.jgdi.mc.map2mc.parser.ChunkBuilder;
import eu.jgdi.mc.map2mc.config.Constants;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.ChunkLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.MinecraftLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.RegionLocation;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.raw.ChunkInfoMap;
import eu.jgdi.mc.map2mc.model.raw.Tuple;

/**
 * Writes the height of each block at surface position (x, z)
 * as a single byte in an intermediate file.
 * The intermediate files are orginazied in regions like the
 * anvil world format. Each region is comprised of 32x32 chunks,
 * and each chunk is comprised of 16x16 blocks.
 *
 * <h2>Data organization</h2>
 * <h3>Chunk</h3>
 * Byte0 corresponds to (0, 0), and byte255 to (15, 15).
 * The bytes are saved line by line, ie. the first 16 bytes
 * correspond to (0..15, 0), the next 16 bytes (0..15, 1) etc.
 * <h3>Region</h3>
 * Byte 0..255 corresponds to chunk0, byte 256..511 to chunk1 etc.
 *
 * <h2>Output</h2>
 * The output filename shall follow the pattern of y-x.
 * For example, the region at (0, 5) will get the name "5-0"
 */
public class IntermediateOutput {

    public static final String FILENAME = "mca-{0}-{1}.tmp";

    public static final Pattern tempFilePattern = Pattern.compile("(\\d+)-(\\d+)");

    private File outputDir;

    public IntermediateOutput(File outputDir) {
        this.outputDir = outputDir;
        try {
            FileUtils.deleteDirectory(outputDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete directory '" + outputDir.getAbsolutePath() + "'");
        }
        if (!outputDir.mkdirs()) {
            throw new RuntimeException("Could not create temporary output directory '" + outputDir.getAbsolutePath() + "'");
        }
    }

    public void writeFiles(List<ChunkBuilder> chunkBuilders) {

        for (ChunkBuilder chunkBuilder : chunkBuilders) {

            ChunkInfoMap chunk;
            try {
                chunk = chunkBuilder.build();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            byte[] chunkData = chunk.getBytes();

            try {
                Tuple<MinecraftLocation> locationTuple = chunk.getLocation().tryReferencedTo(ReferenceFrame.REGION);
                ChunkLocation chunkLocation = (ChunkLocation) locationTuple.first();
                RegionLocation regionLocation = (RegionLocation) locationTuple.second();

                String fileName = MessageFormat.format(FILENAME, regionLocation.getZ(), regionLocation.getX());

                File outputFile = new File(Paths.get(outputDir.getPath(), fileName).toString());

                RandomAccessFile raf = new RandomAccessFile(outputFile, "rw");
                raf.setLength(chunkData.length
                        * Constants.REGION_LEN_X
                        * Constants.REGION_LEN_Z);

                int offset = (chunkLocation.getZ(ReferenceFrame.REGION) * Constants.REGION_LEN_Z * chunkData.length)
                        + (chunkLocation.getX(ReferenceFrame.REGION) * chunkData.length);

                raf.seek(offset);
                raf.write(chunkData);
                raf.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
