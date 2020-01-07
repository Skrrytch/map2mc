package eu.jgdi.mc.map2mc.model.raw;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import eu.jgdi.mc.map2mc.config.WorldConfig;
import eu.jgdi.mc.map2mc.config.WorldRepository;
import eu.jgdi.mc.map2mc.config.csv.SurfaceCsvContent;
import eu.jgdi.mc.map2mc.config.csv.TerrainCsvContent;
import eu.jgdi.mc.map2mc.utils.Logger;
import eu.jgdi.mc.map2mc.utils.Pixels;

public class WorldImageRaster extends WorldRaster {

    private static Logger logger = Logger.logger();

    private final Raster terrainRaster;

    private final Raster surfaceRaster;

    private final Raster mountainsRaster;

    private WorldRepository worldRepo;

    private WorldConfig config;

    private TerrainCsvContent terrainCsvContent;

    private SurfaceCsvContent surfaceCsvContent;

    public WorldImageRaster(WorldRepository worldRepo, BufferedImage terrain, BufferedImage surface, BufferedImage mountains) {
        this.worldRepo = worldRepo;
        this.config = worldRepo.getConfig();
        this.terrainRaster = terrain.getRaster();
        this.surfaceRaster = surface.getRaster();
        this.mountainsRaster = mountains != null ? mountains.getRaster() : null;
        this.terrainCsvContent = config.getTerrainCsvContent();
        this.surfaceCsvContent = config.getSurfaceCsvContent();
        float[] pixel = terrainRaster.getPixel(0, 0, (float[]) null);
        if (pixel.length > 1) {
            throw new IllegalArgumentException("Terrain image is not index based.");
        }
        pixel = surfaceRaster.getPixel(0, 0, (float[]) null);
        if (pixel.length > 1) {
            throw new IllegalArgumentException("Surface image is not index based.");
        }
        if (mountainsRaster!=null) {
            pixel = mountainsRaster.getPixel(0, 0, (float[]) null);
            if (pixel.length > 1) {
                throw new IllegalArgumentException("Mountain image is not index based.");
            }
        }
    }

    public int getWidth() {
        return terrainRaster.getWidth();
    }

    @Override
    public int getHeight() {
        return terrainRaster.getHeight();
    }

    @Override
    public Info getPixelInfo(int pixelX, int pixelY) {
        float[] pixel = terrainRaster.getPixel(pixelX, pixelY, (float[]) null);
        byte terrainColorIndex = Pixels.getColorIndex(pixel);
        byte surfaceColorIndex;
        if (surfaceRaster != terrainRaster) {
            pixel = surfaceRaster.getPixel(pixelX, pixelY, (float[]) null);
            surfaceColorIndex = Pixels.getColorIndex(pixel);
        } else {
            surfaceColorIndex = terrainColorIndex;
        }
        byte mountainColorIndex = 0;
        if (mountainsRaster != null) {
            pixel = mountainsRaster.getPixel(pixelX, pixelY, (float[]) null);
            mountainColorIndex = Pixels.getColorIndex(pixel);
        }
        return new Info(terrainColorIndex, surfaceColorIndex, mountainColorIndex);
    }
}
