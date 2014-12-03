package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.datepicker.client.ui.DateBoxAppended;
import com.github.gwtbootstrap.datepicker.client.ui.base.DateBoxBase;

public class DateBoxAppendedRu extends DateBoxAppended {
    public DateBoxAppendedRu() {
        setLanguage("ru");
        getElement().setAttribute("data-date-weekstart", "1");
        setAutoClose(true);
    }

    public void setPlaceholder(String placeholder) {
        ((DateBoxBase) getWidget(0)).setPlaceholder(placeholder);
    }
    
    public void setTabIndex(int index){
        ((DateBoxBase) getWidget(0)).getElement().setTabIndex(index);
    }
}
