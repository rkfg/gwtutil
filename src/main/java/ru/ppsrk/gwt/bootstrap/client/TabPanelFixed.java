package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.TabLink;
import com.github.gwtbootstrap.client.ui.TabPane;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TabPanel.ShownEvent.Handler;

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

    public TabPaneFixed getSelectedTabPane() {
        return selected;
    }
}
