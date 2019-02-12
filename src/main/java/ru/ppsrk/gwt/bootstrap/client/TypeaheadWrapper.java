package ru.ppsrk.gwt.bootstrap.client;

import static ru.ppsrk.gwt.shared.SharedUtils.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.Typeahead.UpdaterCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import ru.ppsrk.gwt.client.DecoratorBase;
import ru.ppsrk.gwt.client.HasListboxValue;

public class TypeaheadWrapper<T extends HasListboxValue> extends DecoratorBase<Typeahead> {

    public TypeaheadWrapper() {
    }

    public TypeaheadWrapper(Typeahead typeahead) {
        decorated = typeahead;
    }

    /**
     * Fire ValueChangeEvent on the decorated TextBox. This can't be defaulted in the constructor for an unknown reason (maybe the callback
     * gets rewritten due to initialisation order)
     * 
     * @param fire
     *            whether to fire the event or not
     */
    public void setFireChangeEvent(boolean fire) {
        if (fire) {
            decorated.setUpdaterCallback(selectedSuggestion -> {
                final String replacementString = selectedSuggestion.getReplacementString();
                Scheduler.get().scheduleDeferred(() -> ValueChangeEvent.fire(getTextBox(), replacementString));
                return replacementString;
            });
        } else {
            decorated.setUpdaterCallback(Suggestion::getReplacementString);
        }
    }

    public void setUpdaterCallback(UpdaterCallback callback) {
        decorated.setUpdaterCallback(callback);
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
        getTextBox().setValue(text);
    }

    public TextBox getTextBox() {
        return (TextBox) decorated.getWidget();
    }

    public String getText() {
        return getTextBox().getValue();
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
