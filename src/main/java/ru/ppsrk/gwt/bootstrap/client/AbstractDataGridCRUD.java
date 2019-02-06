package ru.ppsrk.gwt.bootstrap.client;

import com.google.gwt.view.client.SingleSelectionModel;

import ru.ppsrk.gwt.client.HasListboxValue;

public abstract class AbstractDataGridCRUD<T extends HasListboxValue> extends AbstractDataGridCRUDBase<T, SingleSelectionModel<T>> {

    public AbstractDataGridCRUD(Class<T> itemsClass, CRUDMessages messages) {
        super(new SingleSelectionModel<>(), itemsClass, messages);
    }
}
