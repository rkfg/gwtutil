package ru.ppsrk.gwt.client;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.Column;

public abstract class ReadonlyCheckboxColumn<T> extends Column<T, Boolean> {

    public ReadonlyCheckboxColumn() {
        super(new CheckboxCell());
    }

    @Override
    public void onBrowserEvent(Context context, Element elem, T object, NativeEvent event) {
        if (BrowserEvents.CHANGE.equals(event.getType())) {
            InputElement input = elem.getFirstChild().cast();
            input.setChecked(!input.isChecked());
        }
    }
}
