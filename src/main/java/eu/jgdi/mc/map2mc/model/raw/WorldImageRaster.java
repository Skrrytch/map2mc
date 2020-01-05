package eu.jgdi.mc.map2mc.model.raw;

import java.awt.image.Raster;

import eu.jgdi.mc.map2mc.utils.Logger;
import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.config.csv.SurfaceCsvContent;
import eu.jgdi.mc.map2mc.config.csv.TerrainCsvContent;
import eu.jgdi.mc.map2mc.utils.Pixels;

public class WorldImageRaster extends WorldRaster {

    private static Logger logger = Logger.logger();

    private final Raster terrainRaster;

    private final Raster surfaceRaster;

    private WorldRepository worldRepo;

    private WorldConfig config;

    private TerrainCsvContent terrainCsvContent;

    private SurfaceCsvContent surfaceCsvContent;

    public WorldImageRaster(Raster terrainRaster, Raster surfaceRaster, WorldRepository worldRepo) {
        this.worldRepo = worldRepo;
        this.config = worldRepo.getConfig();
        this.terrainRaster = terrainRaster;
        this.surfaceRaster = surfaceRaster;
        this.terrainCsvContent = config.getTerrainCsvContent();
        this.surfaceCsvContent = config.getSurfaceCsvContent();
        float[] pixel = terrainRaster.getPixel(0, 0, (float[]) null);
        if (pixel.length > 1) {
            throw new IllegalArgumentException("Image is not index based.");
        }
        pixel = surfaceRaster.getPixel(0, 0, (float[]) null);
        if (pixel.length > 1) {
            throw new IllegalArgumentException("Block type image is not index based.");
        }
    }

    private static float UNDEFINED_PIXEL = 4_294_967_296f;

    public int getWidth() {
        return terrainRaster.getWidth();
    }

    @Override
    public int getHeight() {
        return terrainRaster.getHeight();
    }

    @Override
    public Info getTerrainInfo(int pixelX, int pixelY) {
        float[] pixel = terrainRaster.getPixel(pixelX, pixelY, (float[]) null);
        byte terrainColorIndex = Pixels.getColorIndex(pixel);
        byte surfaceColorIndex = terrainColorIndex;
        if (surfaceRaster != terrainRaster) {
            pixel = surfaceRaster.getPixel(pixelX, pixelY, (float[]) null);
            surfaceColorIndex = Pixels.getColorIndex(pixel);
        }
        return new Info(terrainColorIndex, surfaceColorIndex);
    }
}
