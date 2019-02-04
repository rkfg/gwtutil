package ru.ppsrk.gwt.bootstrap.client;

import static ru.ppsrk.gwt.client.ClientUtils.trySelectionModelValue;

import com.google.gwt.view.client.SingleSelectionModel;

import ru.ppsrk.gwt.client.HasListboxValue;

public abstract class AbstractDataGridCRUD<T extends HasListboxValue> extends AbstractDataGridCRUDBase<T, SingleSelectionModel<T>> {

    public AbstractDataGridCRUD(Class<T> itemsClass, CRUDMessages messages) {
        super(new SingleSelectionModel<>(), itemsClass, messages);
    }

    @Override
    protected T getSelected() {
        return trySelectionModelValue(dg_data.getSelectionModel(), messages.noItemSelected(), itemsClass);
    }

    @Override
    protected void edit(T item) {
        openEditor(item, reloadDataCallback);
    }

}
