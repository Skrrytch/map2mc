package eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe;

import java.util.Objects;

public class FrameTransition {
    public ReferenceFrame from;
    public ReferenceFrame to;

    public FrameTransition(ReferenceFrame from, ReferenceFrame to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FrameTransition) {
            FrameTransition that = (FrameTransition) obj;
            return Objects.equals(this.from, that.from)
                    && Objects.equals(this.to, that.to);
        }
        return super.equals(obj);
    }
}
