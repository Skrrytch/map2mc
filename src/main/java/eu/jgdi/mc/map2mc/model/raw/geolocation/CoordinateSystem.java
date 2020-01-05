package eu.jgdi.mc.map2mc.model.raw.geolocation;

import eu.jgdi.mc.map2mc.model.raw.Tuple;

/**
 * Defines a set of coordinate systems, and the mapping between them and the standard
 * coordinate system of a {@link GeoArea}.
 *
 * @see GeoArea
 */
public enum CoordinateSystem {

    UTM32N(CoordinateSystem::invertedY, 1, -1);

    private CoordinatesMapper coordinatesMapper;
    private double signDirectionX;
    private double signDirectionY;

    CoordinateSystem(CoordinatesMapper coordinatesMapper, double signDirectionX, double signDirectionY) {
        this.coordinatesMapper = coordinatesMapper;
        this.signDirectionX = signDirectionX;
        this.signDirectionY = signDirectionY;
    }

    public Tuple<Coordinate> map(Coordinate minimum, Coordinate maximum) {
        return coordinatesMapper.map(minimum, maximum);
    }

    public double signX(double resolution) {
        return Math.copySign(resolution, signDirectionX);
    }

    public double signY(double resolution) {
        return Math.copySign(resolution, signDirectionY);
    }

    /**
     * Mapping:
     * x -> x
     * y -> inverted y
     */
    static Tuple<Coordinate> invertedY(Coordinate minimum, Coordinate maximum) {
        Coordinate topLeft = new Coordinate(minimum.x, maximum.y);
        Coordinate bottomRight = new Coordinate(maximum.x, minimum.y);
        return new Tuple<>(topLeft, bottomRight);
    }
}

@FunctionalInterface
interface CoordinatesMapper {

    /**
     * Shall deduce the coordinates of the top-left corner and the bottom-right corner,
     * given the extrema of an area.
     *
     * @return <b>first:</b> top-left coordinates, <b>second:</b> bottom-right coordinates.
     */
    Tuple<Coordinate> map(Coordinate minimum, Coordinate maximum);
}