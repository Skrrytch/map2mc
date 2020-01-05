package eu.jgdi.mc.map2mc.model.raw;


import eu.jgdi.mc.map2mc.model.raw.geolocation.GeoArea;

import java.util.function.Supplier;

public class WorldSection {

    private Supplier<WorldRaster> rasterSupplier; // save memory by not loading every raster all at once
    private double resX; // distance unit per pixel, can be negative
    private double resY; // distance unit per pixel, can be negative
    private GeoArea area;

    public WorldSection(Supplier<WorldRaster> rasterSupplier, double resX, double resY, GeoArea area) {
        this.rasterSupplier = rasterSupplier;
        this.resX = resX;
        this.resY = resY;
        this.area = area;
    }

    public GeoArea getArea() {
        return area;
    }

    public WorldRaster getRaster() {
        return rasterSupplier.get();
    }

    public double getResX() {
        return resX;
    }

    public double getResY() {
        return resY;
    }
}
