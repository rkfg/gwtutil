package ru.ppsrk.gwt.client;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class DockLayoutPanelOverflown extends DockLayoutPanel {

    public DockLayoutPanelOverflown(Unit unit) {
        super(unit);
    }

    public void setOverflowingWidget(Widget widget) {
        if (!getChildren().contains(widget)) {
            return;
        }
        widget.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
    }

}
