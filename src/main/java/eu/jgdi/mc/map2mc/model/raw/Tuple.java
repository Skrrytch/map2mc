package eu.jgdi.mc.map2mc.model.raw;

import java.util.Objects;

public class Tuple<T> {

    private T first;
    private T second;

    public Tuple(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public T first() {
        return first;
    }

    public T second() {
        return second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple that = (Tuple) obj;
            return Objects.equals(this.first, that.first)
                    && Objects.equals(this.second, that.second);
        }
        return super.equals(obj);
    }
}
