package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;
import com.github.gwtbootstrap.datepicker.client.ui.base.DateBoxBase;
import com.google.gwt.dom.client.Element;

public class DateBoxAppendedRu extends DateBoxAppended {
    public DateBoxAppendedRu() {
        setLanguage("ru");
        getElement().setAttribute("data-date-weekstart", "1");
        setAutoClose(true);
    }

    public void setPlaceholder(String placeholder) {
        ((DateBoxBase) getWidget(0)).setPlaceholder(placeholder);
    }

    public void setTabIndex(int index) {
        ((DateBoxBase) getWidget(0)).getElement().setTabIndex(index);
    }

    public void setFocus(boolean focus) {
        Element elem = ((DateBoxBase) getWidget(0)).getElement();
        if (focus) {
            elem.focus();
        } else {
            elem.blur();
        }
    }
}
