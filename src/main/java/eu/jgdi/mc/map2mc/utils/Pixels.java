package eu.jgdi.mc.map2mc.utils;

public class Pixels {

    public static byte getColorIndex(float[] pixel) {
        if (pixel.length == 1) { // index based palette
            return (byte) pixel[0];
        }
        throw new IllegalArgumentException("Pixel array with unexpected size " + pixel.length + ". Expected 1!");
    }
}
