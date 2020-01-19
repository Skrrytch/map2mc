package eu.jgdi.mc.map2mc.model.raw;

public abstract class WorldRaster {

    public static class Info {

        private byte terrainIndex;

        private byte surfaceIndex;

        private byte mountainIndex;

        private byte biomeIndex;

        public Info(byte terrainIndex, byte surfaceIndex, byte mountainIndex, byte biomeIndex) {
            this.terrainIndex = terrainIndex;
            this.surfaceIndex = surfaceIndex;
            this.mountainIndex = mountainIndex;
            this.biomeIndex = biomeIndex;
        }

        public byte getTerrainIndex() {
            return terrainIndex;
        }

        public byte getSurfaceIndex() {
            return surfaceIndex;
        }

        public byte getMountainIndex() {
            return mountainIndex;
        }

        public byte getBiomeIndex() {
            return biomeIndex;
        }
    }

    public abstract int getHeight();

    public abstract int getWidth();

    public abstract Info getPixelInfo(int pixelX, int pixelY);
}
