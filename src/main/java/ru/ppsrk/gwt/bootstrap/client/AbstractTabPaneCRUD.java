package ru.ppsrk.gwt.bootstrap.client;

import com.google.gwt.view.client.SingleSelectionModel;

import ru.ppsrk.gwt.client.HasListboxValue;

public abstract class AbstractTabPaneCRUD<T extends HasListboxValue> extends AbstractTabPaneCRUDBase<T, SingleSelectionModel<T>> {

    public AbstractTabPaneCRUD(Class<T> itemsClass, CRUDMessages messages) {
        super(new SingleSelectionModel<>(), itemsClass, messages);
    }
}
