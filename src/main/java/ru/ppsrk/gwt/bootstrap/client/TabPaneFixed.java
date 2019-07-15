package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.TabPane;
import com.google.gwt.user.client.ui.Widget;

public class TabPaneFixed extends TabPane {

    protected void initWidget(Widget widget, String heading) {
        setHeight("100%");
        setHeading(heading);
        add(widget);
    }

    protected void onShown() {
        // empty by default
    }

}
