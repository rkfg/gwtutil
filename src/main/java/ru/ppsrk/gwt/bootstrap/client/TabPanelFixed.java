package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPane;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TabPanel.ShownEvent.Handler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Element;

@SuppressWarnings("deprecation")
public class TabPanelFixed extends TabPanel {

    private TabPaneFixed selected = null;

    public TabPanelFixed() {
        addShownHandler(new Handler() {

            @Override
            public void onShow(ShownEvent shownEvent) {
                TabLink target = shownEvent.getTarget();
                if (target != null) {
                    TabPane tabPane = target.getTabPane();
                    if (tabPane instanceof TabPaneFixed) {
                        ((TabPaneFixed) tabPane).onShown();
                        selected = (TabPaneFixed) tabPane;
                    }
                }
            }
        });
    }

    public void recalculateSize() {
        int height = ((Element) getElement().getFirstChild()).getOffsetHeight();
        Element element = (Element) getElement().getFirstChild().getNextSibling();
        element.getStyle().setHeight(getOffsetHeight() - height - 10, Unit.PX);
    }

    public TabPaneFixed getSelectedTabPane() {
        return selected;
    }
}
