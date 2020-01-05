package eu.jgdi.mc.map2mc.utils;

import java.util.function.Supplier;

public class LazyGet<T> implements Supplier<T> {

    private T value = null;
    private Supplier<T> supplier;

    public LazyGet (Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }
}
