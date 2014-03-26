package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;

public class DateBoxAppendedRu extends DateBoxAppended {
    public DateBoxAppendedRu() {
        setLanguage("ru");
        getElement().setAttribute("data-date-weekstart", "1");
        setAutoClose(true);
    }
}
