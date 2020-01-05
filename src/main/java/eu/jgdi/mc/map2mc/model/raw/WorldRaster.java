package eu.jgdi.mc.map2mc.model.raw;

public abstract class WorldRaster {

    public static class Info {

        private byte terrainIndex;

        private byte surfaceIndex;

        public Info(byte terrainIndex, byte surfaceIndex) {
            this.terrainIndex = terrainIndex;
            this.surfaceIndex = surfaceIndex;
        }

        public byte getTerrainIndex() {
            return terrainIndex;
        }

        public byte getSurfaceIndex() {
            return surfaceIndex;
        }
    }

    public abstract int getHeight();

    public abstract int getWidth();

    public abstract Info getTerrainInfo(int pixelX, int pixelY);
}
