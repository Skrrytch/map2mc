package eu.jgdi.mc.map2mc.model.minecraft.coordinates.referenceframe;

import eu.jgdi.mc.map2mc.model.minecraft.coordinates.MinecraftLocation;
import eu.jgdi.mc.map2mc.model.raw.Tuple;

@FunctionalInterface
public interface ReferenceFrameShifter {

    /**
     * @param instance The location we are reference shifting.
     * @return A tuple where:<br>
     *          first: Location of same type as input, but reference shifted.<br>
     *          second: Location of same type as the reference. Referenced to world.
     */
    Tuple<MinecraftLocation> shift(MinecraftLocation instance);
}
