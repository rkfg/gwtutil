package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.NavTabs;
import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPane;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;

public class TabPanelFixed extends TabPanel {

    private TabPaneFixed selected = null;

    public TabPanelFixed() {
        addShownHandler(shownEvent -> {
            TabLink target = shownEvent.getTarget();
            if (target != null) {
                TabPane tabPane = target.getTabPane();
                if (tabPane instanceof TabPaneFixed) {
                    ((TabPaneFixed) tabPane).onShown();
                    selected = (TabPaneFixed) tabPane;
                }
            }
        });
    }

    public void recalculateSize() {
        int height = getElement().getFirstChildElement().getOffsetHeight();
        Element element = getElement().getFirstChildElement().getNextSiblingElement();
        element.getStyle().setHeight(getOffsetHeight() - height - 10., Unit.PX);
    }

    public TabPaneFixed getSelectedTabPane() {
        return selected;
    }

    public void setTabVisible(int index, boolean visible) {
        ((NavTabs) getWidget(0)).getWidget(index).setVisible(visible);
    }
}
