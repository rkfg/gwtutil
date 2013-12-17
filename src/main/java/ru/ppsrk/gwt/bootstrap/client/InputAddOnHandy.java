package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.InputAddOn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class InputAddOnHandy extends InputAddOn {

    public InputAddOnHandy() {
        super();
        addStyleName("input-append");
    }

    public InputAddOnHandy(Widget... widgets) {
        this();
        for (Widget widget : widgets) {
            add(widget);
        }
    }
    public InputAddOnHandy(IsWidget... widgets) {
        this();
        for (IsWidget widget : widgets) {
            add(widget);
        }
    }
}
