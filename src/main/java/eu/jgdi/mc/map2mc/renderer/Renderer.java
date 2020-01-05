package eu.jgdi.mc.map2mc.renderer;

import eu.jgdi.mc.map2mc.model.minecraft.coordinates.RegionLocation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public abstract class Renderer {

    public abstract void render();

    /**
     * Maps all <i>valid</i> intermediate output files to a {@link RegionLocation}
     */
    Map<File, RegionLocation> mapToRegions(List<File> intermediateFiles) {

        Map<File, RegionLocation> map = new HashMap<>();

        for (File file : intermediateFiles) {

            Matcher matcher = IntermediateOutput.tempFilePattern.matcher(file.getName());
            if (matcher.find()) {
                RegionLocation regionLocation = new RegionLocation(
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(1)));

                map.put(file, regionLocation);
            }
            else {
                System.out.println(file.getName() + ": illegal file name");
            }
        }

        return map;
    }
}
