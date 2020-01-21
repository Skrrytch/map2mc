package eu.jgdi.mc.map2mc.model.raw;

import java.util.function.Supplier;

public class WorldSection {

    private Supplier<WorldRaster> rasterSupplier; // save memory by not loading every raster all at once

    public WorldSection(Supplier<WorldRaster> rasterSupplier) {
        this.rasterSupplier = rasterSupplier;
    }

    public WorldRaster getRaster() {
        return rasterSupplier.get();
    }
}
