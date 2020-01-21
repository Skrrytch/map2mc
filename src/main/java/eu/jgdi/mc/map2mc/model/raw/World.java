package eu.jgdi.mc.map2mc.model.raw;

public class World {

    private WorldSection worldSection;

    public World(WorldSection worldSection) {
        this.worldSection = worldSection;
    }

    public WorldSection getSection() {
        return worldSection;
    }
}
