package ru.ppsrk.gwt.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import ru.ppsrk.gwt.client.HasListboxValue;

public class ListBoxDecorator<T extends HasListboxValue> extends DecoratorBase<ListBox> {
    private Map<Long, T> map = new HashMap<>();

    public ListBoxDecorator() {
    }

    public ListBoxDecorator(ListBox listBox) {
        decorated = listBox;
    }

    public void insertItem(T value, int index) {
        decorated.insertItem(value.getListboxValue(), value.getId().toString(), index);
    }

    public void addItem(T value) {
        decorated.addItem(value.getListboxValue(), value.getId().toString());
    }

    public void fill(Collection<T> list) {
        for (T item : list) {
            map.put(item.getId(), item);
        }
        decorated.clear();
        for (T listboxValue : list) {
            addItem(listboxValue);
        }
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
    protected void checkType(Widget w) {
        if (!(w instanceof ListBox)) {
            throw new IllegalArgumentException("Only ListBox is allowed as a child widget.");
        }
    }

}
