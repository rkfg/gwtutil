package ru.ppsrk.gwt.shared;

import java.util.Comparator;
import java.util.function.Function;

/**
 * A comparator to sort objects using a single field that's usually obtained via getter method reference.
 * @author rkfg
 *
 * @param <T> object type
 * @param <F> field type (must implement {@link Comparable})
 */
public class FieldComparator<T, F extends Comparable<F>> implements Comparator<T> {

    private Function<T, F> f;

    public FieldComparator(Function<T, F> f) {
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
