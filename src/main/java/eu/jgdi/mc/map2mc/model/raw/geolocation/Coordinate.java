package eu.jgdi.mc.map2mc.model.raw.geolocation;

import java.awt.geom.Point2D;

public class Coordinate extends Point2D.Double {

    public Coordinate(double x, double y) {
        super(x, y);
    }

    @Override
    public String toString() {
        return "(x: " + x + ", y: " + y + ")";
    }
}
