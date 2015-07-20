package ru.ppsrk.gwt.bootstrap.client;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public abstract class ContainerTab extends TabPaneFixed {

    private static ContainerTabUiBinder uiBinder = GWT.create(ContainerTabUiBinder.class);

    interface ContainerTabUiBinder extends UiBinder<Widget, ContainerTab> {
    }

    @UiField(provided = true)
    protected TabPanelFixed tp_main = new TabPanelFixed();

    public ContainerTab(String header) {
        initWidget(uiBinder.createAndBindUi(this), header);
        for (AbstractDataGridTabPane<?> tab : getTabs()) {
            tp_main.add(tab);
        }
        tp_main.selectTab(0);
    }

    protected abstract Collection<? extends AbstractDataGridTabPane<?>> getTabs();

    @Override
    protected void onShown() {
        ((AbstractDataGridTabPane<?>) tp_main.getSelectedTabPane()).onShown();
    }

}
