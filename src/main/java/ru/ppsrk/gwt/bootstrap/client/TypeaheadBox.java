package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.Typeahead.UpdaterCallback;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public abstract class TypeaheadBox extends TextBox {

    private Typeahead typeahead = new Typeahead();
    private int suggestOnLength;
    private boolean enterAllowed = true;

    public TypeaheadBox() {
        this(3);
    }

    public TypeaheadBox(int suggestOnLength) {
        super();
        addKeyPressHandler(new ThisKeyPressHandler());
        typeahead.add(this);
        typeahead.setUpdaterCallback(new UpdaterCallback() {

            @Override
            public String onSelection(Suggestion selectedSuggestion) {
                enterAllowed = true;
                return updater(selectedSuggestion);
            }
        });
    }

    protected String updater(Suggestion selectedSuggestion) {
        return selectedSuggestion.getReplacementString();
    }

    public Typeahead getTypeahead() {
        return typeahead;
    }

    public MultiWordSuggestOracle getSuggestOracle() {
        return (MultiWordSuggestOracle) typeahead.getSuggestOracle();
    }

    private class ThisKeyPressHandler implements KeyPressHandler {
        public void onKeyPress(KeyPressEvent event) {
            if (event.getNativeEvent().getCharCode() == KeyCodes.KEY_ENTER && enterAllowed) {
                doEnter();
            }
            if (getValue().length() >= suggestOnLength) {
                enterAllowed = false;
                fillOracle();
            }
        }
    }

    public abstract void fillOracle();

    public void doEnter() {
        
    }
}
