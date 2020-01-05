package eu.jgdi.mc.map2mc.model.minecraft.coordinates;

import eu.jgdi.mc.map2mc.model.raw.Tuple;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.FrameTransition;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrame;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrameException;
import eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe.ReferenceFrameShifter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Describes a location in Minecraft in relation to the whole world (default),
 * or in relation to another {@link ReferenceFrame}.
 * <br><br>
 * It is an error to operate on locations of differing reference frames, and
 * thus this base class provides strict checking of reference frames.
 */
public abstract class MinecraftLocation {

    int x;
    int z;
    ReferenceFrame referenceFrame;

    /**
     * A map for looking up <b>reference frame shifters</b> given a <b>frame transition</b>.
     *
     * @see FrameTransition
     * @see ReferenceFrameShifter
     */
    abstract Map<FrameTransition, ReferenceFrameShifter> getReferenceShifters();

    public MinecraftLocation(int x, int z) {
        this.referenceFrame = ReferenceFrame.WORLD;
        this.x = x;
        this.z = z;
    }

    public MinecraftLocation(int x, int z, ReferenceFrame referenceFrame) {
        this.referenceFrame = referenceFrame;
        this.x = x;
        this.z = z;
    }

    private int getComponent(ReferenceFrame referenceFrame,
                             Function<MinecraftLocation, Integer> getter) {

        if (this.referenceFrame != referenceFrame) {
            return getter.apply(this.tryReferencedTo(referenceFrame).first());
        }
        return getter.apply(this);
    }

    public int getX() {
        return getX(ReferenceFrame.WORLD);
    }

    public int getX(ReferenceFrame referenceFrame) {
        return getComponent(referenceFrame, location -> location.x);
    }

    public int getZ() {
        return getZ(ReferenceFrame.WORLD);
    }

    public int getZ(ReferenceFrame referenceFrame) {
        return getComponent(referenceFrame, location -> location.z);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public ReferenceFrame getReferenceFrame() {
        return referenceFrame;
    }

    public Optional<Tuple<MinecraftLocation>> referencedTo(ReferenceFrame referenceFrame) {

        Map<FrameTransition, ReferenceFrameShifter> referenceShifters = this.getReferenceShifters();
        FrameTransition transition = new FrameTransition(this.referenceFrame, referenceFrame);

        if (referenceShifters.containsKey(transition)) {
            return Optional.of(referenceShifters.get(transition).shift(this));
        }

        return Optional.empty();
    }

    public Tuple<MinecraftLocation> tryReferencedTo(ReferenceFrame referenceFrame) {
        return referencedTo(referenceFrame).orElseThrow(() ->
                new ReferenceFrameException(this, referenceFrame));
    }

    public boolean isAbsolutePosition() {
        return referenceFrame == ReferenceFrame.WORLD;
    }

    public boolean isRelativePosition() {
        return !isAbsolutePosition();
    }

    @Override
    public String toString() {
        return "(x: " + x + ", z: " + z + ")" + (isAbsolutePosition() ? "" : " in " + this.referenceFrame);
    }


    /**
     * Stolen from {@link java.awt.geom.Point2D#hashCode()}
     */
    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(x);
        bits ^= Double.doubleToLongBits(z) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Modified from {@link java.awt.geom.Point2D#equals(Object)}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MinecraftLocation) {
            MinecraftLocation that = (MinecraftLocation) obj;
            if (this.referenceFrame != that.referenceFrame) {
                throw new ReferenceFrameException("Tried to compare two locations of differing reference frames");
            }
            return (x == that.x) && (z == that.z);
        }
        return super.equals(obj);
    }
}
