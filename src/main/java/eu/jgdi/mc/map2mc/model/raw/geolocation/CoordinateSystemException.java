package eu.jgdi.mc.map2mc.model.raw.geolocation;

public class CoordinateSystemException extends RuntimeException {

    public CoordinateSystemException(GeoArea area1, GeoArea area2) {
        super(String.format(
                "Cannot operate on differing coordinate systems (%s:%s vs. %s:%s)",
                area1.getGeodeticDatum(),
                area1.getCoordinateSystem(),
                area2.getGeodeticDatum(),
                area2.getCoordinateSystem()));
    }
}
