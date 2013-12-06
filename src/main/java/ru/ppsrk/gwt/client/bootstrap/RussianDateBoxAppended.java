package ru.ppsrk.gwt.client.bootstrap;

import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;

public class RussianDateBoxAppended extends DateBoxAppended {

    public RussianDateBoxAppended() {
        super();
        setLanguage("ru");
        setFormat("dd.mm.yyyy");
        setAutoClose(true);
    }

}
