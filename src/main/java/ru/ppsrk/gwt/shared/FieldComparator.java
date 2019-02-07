package ru.ppsrk.gwt.shared;

import java.util.Comparator;
import java.util.function.Function;

public class FieldComparator<T, C extends Comparable<C>> implements Comparator<T> {

    private Function<T, C> f;

    public FieldComparator(Function<T, C> f) {
        this.f = f;
    }

    @Override
    public int compare(T o1, T o2) {
        if (f.apply(o1) == null) {
            return -1;
        }
        if (f.apply(o2) == null) {
            return 1;
        }
        return f.apply(o1).compareTo(f.apply(o2));
    }

}
