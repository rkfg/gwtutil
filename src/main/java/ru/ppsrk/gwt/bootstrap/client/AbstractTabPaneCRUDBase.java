package ru.ppsrk.gwt.bootstrap.client;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SetSelectionModel;

import ru.ppsrk.gwt.client.HasListboxValue;

public abstract class AbstractTabPaneCRUDBase<T extends HasListboxValue, S extends SetSelectionModel<T>> extends AbstractDataGridCRUDBase<T, S> {
    protected TabPaneFixed tabPane;

    public AbstractTabPaneCRUDBase(S selectionModel, Class<T> itemsClass, CRUDMessages messages) {
        super(selectionModel, itemsClass, messages);
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
