package ru.ppsrk.gwt.bootstrap.client;

import static ru.ppsrk.gwt.shared.SharedUtils.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;

import ru.ppsrk.gwt.client.DecoratorBase;
import ru.ppsrk.gwt.client.HasListboxValue;

public class TypeaheadWrapper<T extends HasListboxValue> extends DecoratorBase<Typeahead> {

    public TypeaheadWrapper() {
    }

    public TypeaheadWrapper(Typeahead typeahead) {
        decorated = typeahead;
    }

    private Map<String, T> objects = new HashMap<>();

    public void fill(Collection<T> data) {
        MultiWordSuggestOracle suggestOracle = (MultiWordSuggestOracle) decorated.getSuggestOracle();
        suggestOracle.clear();
        objects.clear();
        for (T elem : data) {
            String title = elem.getListboxValue();
            suggestOracle.add(title);
            objects.put(title, elem);
        }
    }

    public T getValue() {
        return objects.get(getText());
    }

    public void setText(String text) {
        ((TextBox) decorated.getWidget()).setValue(text);
    }

    public String getText() {
        return ((TextBox) decorated.getWidget()).getValue();
    }

    @Override
    protected void checkType(IsWidget w) {
        if (!(w instanceof Typeahead)) {
            throw new IllegalArgumentException("Only Typeahead is allowed as a child widget.");
        }
    }

    public void setSelectedId(Long id) {
        T object = getObjectFromCollectionById(objects.values(), id);
        if (object != null) {
            setText(object.getListboxValue());
        }
    }

    public Long getSelectedId() {
        T value = getValue();
        if (value != null) {
            return value.getId();
        }
        return null;
    }
}
