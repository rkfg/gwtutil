package ru.ppsrk.gwt.bootstrap.client;

import com.google.gwt.user.client.ui.Widget;

import ru.ppsrk.gwt.client.HasListboxValue;

public abstract class AbstractTabPaneCRUD<T extends HasListboxValue> extends AbstractDataGridCRUD<T> {

    protected TabPaneFixed tabPane;

    public AbstractTabPaneCRUD(Class<T> itemsClass, CRUDMessages messages) {
        super(itemsClass, messages);
        tabPane = new TabPaneFixed() {
            @Override
            protected void onShown() {
                dg_data.redraw();
            }
        };
    }

    protected void initWidget(Widget widget, String heading) {
        tabPane.initWidget(widget, heading);
    }

    @Override
    public TabPaneFixed asWidget() {
        return tabPane;
    }
    
}
