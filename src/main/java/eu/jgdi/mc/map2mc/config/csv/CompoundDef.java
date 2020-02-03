package eu.jgdi.mc.map2mc.config.csv;

import net.querz.nbt.CompoundTag;

public class CompoundDef {

    private String id;

    private CompoundTag compoundTag;

    private boolean air = false;

    private boolean undefined = false;

    public CompoundDef(String id, CompoundTag compoundTag) {
        this.id = id;
        this.compoundTag = compoundTag;
    }

    public String getId() {
        return id;
    }

    public CompoundTag getCompoundTag() {
        return compoundTag;
    }

    public void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    public void setAir(boolean air) {
        this.air = air;
    }

    public boolean isUndefined() {
        return undefined;
    }

    public boolean isAir() {
        return air;
    }
}
