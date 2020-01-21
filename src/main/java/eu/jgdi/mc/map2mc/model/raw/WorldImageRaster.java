package eu.jgdi.mc.map2mc.model.raw;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import eu.jgdi.mc.map2mc.utils.Logger;

public class WorldImageRaster extends WorldRaster {

    private static final Logger logger = Logger.logger();

    private final Raster terrainRaster;

    private final Raster surfaceRaster;

    private final Raster mountainsRaster;

    private final Raster biomeRaster;

    public WorldImageRaster(
            BufferedImage terrain,
            BufferedImage surface,
            BufferedImage mountains,
            BufferedImage biomes) {
        this.terrainRaster = terrain.getRaster();
        this.surfaceRaster = surface.getRaster();
        this.mountainsRaster = mountains != null ? mountains.getRaster() : null;
        this.biomeRaster = biomes != null ? biomes.getRaster() : null;
        float[] pixel = terrainRaster.getPixel(0, 0, (float[]) null);
        if (pixel.length > 1) {
            throw new IllegalArgumentException("Terrain image is not index based.");
        }
        pixel = surfaceRaster.getPixel(0, 0, (float[]) null);
        if (pixel.length > 1) {
            throw new IllegalArgumentException("Surface image is not index based.");
        }
        if (mountainsRaster != null) {
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
        byte terrainColorIndex = (byte) pixel[0];
        byte surfaceColorIndex;
        if (surfaceRaster != terrainRaster) {
            pixel = surfaceRaster.getPixel(pixelX, pixelY, (float[]) null);
            surfaceColorIndex = (byte) pixel[0];
        } else {
            surfaceColorIndex = terrainColorIndex;
        }
        byte mountainColorIndex = 0;
        if (mountainsRaster != null) {
            pixel = mountainsRaster.getPixel(pixelX, pixelY, (float[]) null);
            mountainColorIndex = (byte) pixel[0];
        }
        byte biomeColorIndex = (byte) 0;
        if (biomeRaster != null) {
            pixel = biomeRaster.getPixel(pixelX, pixelY, (float[]) null);
            biomeColorIndex = (byte) pixel[0];
        }
        return new Info(terrainColorIndex, surfaceColorIndex, mountainColorIndex, biomeColorIndex);
    }
}
