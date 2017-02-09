package ru.ppsrk.gwt.bootstrap.client;

import java.util.Collection;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.Typeahead;
import com.github.gwtbootstrap.client.ui.Typeahead.HighlighterCallback;
import com.github.gwtbootstrap.client.ui.Typeahead.UpdaterCallback;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

public abstract class TypeaheadBox extends TextBox implements PreventsKeyboardClose {

    private Typeahead typeahead = new Typeahead();
    private int suggestOnLength;
    private boolean enterAllowed = true;
    private KeyboardClosable parentPanel;

    public TypeaheadBox() {
        this(3);
    }

    public TypeaheadBox(int suggestOnLength) {
        super();
        this.suggestOnLength = suggestOnLength;
        addKeyPressHandler(new ThisKeyPressHandler());
        typeahead.add(this);
        typeahead.setUpdaterCallback(new UpdaterCallback() {

            @Override
            public String onSelection(Suggestion selectedSuggestion) {
                enterAllowed = true;
                return updater(selectedSuggestion);
            }
        });
        typeahead.setHighlighterCallback(new HighlighterCallback() {
            
            @Override
            public String highlight(String item) {
                return highlighter(item);
            }
        });
        addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                if (parentPanel == null) {
                    return;
                }
                parentPanel.setFocusedWidget(TypeaheadBox.this);
            }
        });
        addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                if (parentPanel == null) {
                    return;
                }
                parentPanel.setFocusedWidget(null);
            }
        });
        addAttachHandler(new Handler() {

            @Override
            public void onAttachOrDetach(AttachEvent event) {
                parentPanel = getParentClosablePanel();
            }
        });
        typeahead.reconfigure();
    }

    protected String highlighter(String item) {
        return item;
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
            if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && enterAllowed) {
                doEnter();
            }
            if (getValue().length() >= suggestOnLength) {
                enterAllowed = false;
                fillOracle();
            }
        }
    }

    protected abstract void fillOracle();

    public void doEnter() {
        if (parentPanel != null) {
            parentPanel.closeOk();
        }
    }

    private KeyboardClosable getParentClosablePanel() {
        Widget parent = this;
        while ((parent = parent.getParent()) != null) {
            if (parent instanceof KeyboardClosable) {
                return ((KeyboardClosable) parent);
            }
        }
        return null;
    }

    protected void setSuggestOracleContents(Collection<String> suggestions) {
        getSuggestOracle().clear();
        getSuggestOracle().addAll(suggestions);
    }
}
