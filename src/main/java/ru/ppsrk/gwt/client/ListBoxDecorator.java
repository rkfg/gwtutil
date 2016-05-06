package ru.ppsrk.gwt.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.ListBox;

public class ListBoxDecorator<T extends HasListboxValue> {
    private ListBox listBox;
    private Map<Long, T> map = new HashMap<>();

    public ListBoxDecorator(ListBox listBox) {
        this.listBox = listBox;
    }

    public void addItem(HasListboxValue value) {
        listBox.addItem(value.getListboxValue(), value.getId().toString());
    }

    public void fill(Collection<T> list) {
        for (T item : list) {
            map.put(item.getId(), item);
        }
        listBox.clear();
        for (HasListboxValue listboxValue : list) {
            addItem(listboxValue);
        }
    }

    public int getIndexByLong(Long value) {
        return getIndexByTextValue(value.toString());
    }

    public int getIndexByTextValue(String value) {
        for (int i = 0; i < listBox.getItemCount(); i++) {
            if (listBox.getValue(i).equals(value)) {
                return i;
            }
        }
        return -1;
    };

    public String getSelectedText() {
        return listBox.getSelectedIndex() >= 0 ? listBox.getItemText(listBox.getSelectedIndex()) : "";
    };

    public String getSelectedTextValue() {
        return listBox.getSelectedIndex() >= 0 ? listBox.getValue(listBox.getSelectedIndex()) : null;
    }

    public Long getSelectedLong() {
        return listBox.getSelectedIndex() >= 0 ? Long.valueOf(listBox.getValue(listBox.getSelectedIndex())) : -1;
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
        listBox.setSelectedIndex(getIndexByLong(value));
    }

    public void setSelectedItemByLong(String value) {
        listBox.setSelectedIndex(getIndexByTextValue(value));
    }

    public <E extends Enum<E>> void setListBoxSelectedEnum(E e) {
        setSelectedItemByLong((long) e.ordinal());
    }
}
