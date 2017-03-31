package ru.ppsrk.gwt.client;

import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import elemental.client.Browser;
import ru.ppsrk.gwt.client.DTOSuggestBox.Suggestable;

public class DTOSuggestBox<T extends Suggestable> extends SuggestBox {

    private T selected;

    public interface Suggestable extends HasListboxValue {

        public String getReplacementString();
    }

    public interface RemoteSuggestionCallback<T extends Suggestable> {
        public void requestRemoteSuggestions(Request request, AsyncCallback<List<T>> callback);
    }

    public DTOSuggestBox() {
        super(new DTOSuggestOracle<T>());
        addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

            @SuppressWarnings("unchecked")
            @Override
            public void onSelection(SelectionEvent<Suggestion> event) {
                selected = ((DTOSuggestion<T>) event.getSelectedItem()).getDTO();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public DTOSuggestOracle<T> getSuggestOracle() {
        return (DTOSuggestOracle<T>) super.getSuggestOracle();
    }

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
        setValue(selected.getReplacementString());
    }

    public void setRemoteSuggestionCallback(RemoteSuggestionCallback<T> rsCallback) {
        getSuggestOracle().setRemoteSuggestionCallback(rsCallback);
    }

    /**
     * Use this to prevent modals from closing on selecting a suggestion with keyboard (Enter). If a SuggestBox is selected, don't react on
     * Enter.
     * 
     * @return whether this element has focus
     */
    public boolean isFocused() {
        return Browser.getDocument().getActiveElement() == getElement();
    }

}
