package ru.ppsrk.gwt.bootstrap.client;

import static ru.ppsrk.gwt.client.ClientUtils.trySelectionModelValue;

import com.google.gwt.view.client.SingleSelectionModel;

import ru.ppsrk.gwt.client.HasListboxValue;

public abstract class AbstractTabPaneCRUD<T extends HasListboxValue> extends AbstractTabPaneCRUDBase<T, SingleSelectionModel<T>> {

    public AbstractTabPaneCRUD(Class<T> itemsClass, CRUDMessages messages) {
        super(new SingleSelectionModel<>(), itemsClass, messages);
    }

    @Override
    protected void edit(T item) {
        openEditor(item, reloadDataCallback);
    }

    @Override
    protected T getSelected() {
        return trySelectionModelValue(dg_data.getSelectionModel(), messages.noItemSelected(), itemsClass);
    }
    
}
