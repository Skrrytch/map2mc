package eu.jgdi.mc.map2mc.model.raw;


import eu.jgdi.mc.map2mc.model.raw.geolocation.GeoArea;

import java.util.function.Supplier;

public class WorldSection {

    private Supplier<WorldRaster> rasterSupplier; // save memory by not loading every raster all at once
    private GeoArea area;

    public WorldSection(Supplier<WorldRaster> rasterSupplier, GeoArea area) {
        this.rasterSupplier = rasterSupplier;
        this.area = area;
    }

    public GeoArea getArea() {
        return area;
    }

    public WorldRaster getRaster() {
        return rasterSupplier.get();
    }
}
