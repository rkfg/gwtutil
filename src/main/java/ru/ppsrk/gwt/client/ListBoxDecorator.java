package ru.ppsrk.gwt.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;

public class ListBoxDecorator<T extends HasListboxValue> extends DecoratorBase<ListBox> {
    private Map<Long, T> map = new HashMap<>();

    public ListBoxDecorator() {
    }

    public ListBoxDecorator(ListBox listBox) {
        decorated = listBox;
    }

    public void insertItem(T value, int index) {
        decorated.insertItem(value.getListboxValue(), value.getId().toString(), index);
        map.put(value.getId(), value);
    }

    public void addItem(T value) {
        decorated.addItem(value.getListboxValue(), value.getId().toString());
        map.put(value.getId(), value);
    }

    public void addAll(Collection<T> values) {
        for (T value : values) {
            addItem(value);
        }
    }

    public void fill(Collection<T> values) {
        removeAll();
        addAll(values);
    }

    public void removeAll() {
        decorated.clear();
    }

    public int getIndexByLong(Long value) {
        return getIndexByTextValue(value.toString());
    }

    public int getIndexByTextValue(String value) {
        for (int i = 0; i < decorated.getItemCount(); i++) {
            if (decorated.getValue(i).equals(value)) {
                return i;
            }
        }
        return -1;
    };

    public String getSelectedText() {
        return decorated.getSelectedIndex() >= 0 ? decorated.getItemText(decorated.getSelectedIndex()) : "";
    };

    public String getSelectedTextValue() {
        return decorated.getSelectedIndex() >= 0 ? decorated.getValue(decorated.getSelectedIndex()) : null;
    }

    public Long getSelectedLong() {
        return decorated.getSelectedIndex() >= 0 ? Long.valueOf(decorated.getValue(decorated.getSelectedIndex())) : -1;
    }

    public <E extends Enum<E>> E getSelectedEnum(E[] values) {
        return values[getSelectedLong().intValue()];
    }

    public T getSelectedValue() {
        if (map == null) {
            return null;
        }
        return map.get(getSelectedLong());
    }

    public void setSelectedItemByLong(Long value) {
        decorated.setSelectedIndex(getIndexByLong(value));
    }

    public void setSelectedItemByLong(String value) {
        decorated.setSelectedIndex(getIndexByTextValue(value));
    }

    public <E extends Enum<E>> void setListBoxSelectedEnum(E e) {
        setSelectedItemByLong((long) e.ordinal());
    }

    @Override
    protected void checkType(IsWidget w) {
        if (!(w instanceof ListBox)) {
            throw new IllegalArgumentException("Only ListBox is allowed as a child widget.");
        }
    }

    public Set<T> getSelectedValues() {
        Set<T> result = new HashSet<>();
        for (int i = 0; i < decorated.getItemCount(); i++) {
            if (decorated.isItemSelected(i)) {
                result.add(map.get(Long.valueOf(decorated.getValue(i))));
            }
        }
        return result;
    }

    public void removeValue(T value) {
        Long id = value.getId();
        decorated.removeItem(getIndexByLong(id));
        map.remove(id);
    }

    public void removeValues(Collection<T> values) {
        for (T val : values) {
            removeValue(val);
        }
    }

    public Collection<T> getValues() {
        return map.values();
    }
}
