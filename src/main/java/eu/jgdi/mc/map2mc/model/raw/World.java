package eu.jgdi.mc.map2mc.model.raw;

import eu.jgdi.mc.map2mc.utils.LazyGet;
import eu.jgdi.mc.map2mc.model.raw.geolocation.GeoArea;

import java.util.List;

public class World {

    private List<WorldSection> worldSections;

    private LazyGet<GeoArea> area = new LazyGet<>(
            () -> worldSections.stream()
                    .map(WorldSection::getArea)
                    .reduce(GeoArea::makeContainer)
                    .orElseThrow(() -> new RuntimeException("error while processing world sections"))
    );

    public World(List<WorldSection> worldSections) {
        this.worldSections = worldSections;
    }

    public GeoArea getArea() {
        return area.get();
    }

    public List<WorldSection> getSections() {
        return worldSections;
    }
}
