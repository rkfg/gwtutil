package ru.ppsrk.gwt.bootstrap.client;

import static ru.ppsrk.gwt.client.ClientUtils.*;
import ru.ppsrk.gwt.client.ClientUtils.MyAsyncCallback;
import ru.ppsrk.gwt.client.HasListboxValue;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractDataGridCRUD<T extends HasListboxValue> extends AbstractDataGridTabPane<T> {

    private CRUDMessages messages;
    private Class<T> itemsClass;

    private MyAsyncCallback<Void> reloadDataCallback = new MyAsyncCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
            loadData();
        }
    };

    public interface CRUDMessages extends Messages {

        @DefaultMessage("No item selected")
        String noItemSelected();

        @DefaultMessage("Really delete the item {0}?")
        String confirmItemDeletion(String itemText);

    }

    public AbstractDataGridCRUD(Class<T> itemsClass, CRUDMessages messages) {
        this.itemsClass = itemsClass;
        this.messages = messages;
    }

    @UiHandler("b_add")
    public void onAddClick(ClickEvent e) {
        edit(null);
    }

    @UiHandler("b_change")
    public void onChangeClick(ClickEvent e) {
        edit(getSelected());
    }

    @UiHandler("b_del")
    public void onDelPartClick(ClickEvent e) {
        T selectedPart = getSelected();
        if (!Window.confirm(messages.confirmItemDeletion(selectedPart.getListboxValue()))) {
            return;
        }
        deleteItem(selectedPart.getId(), reloadDataCallback);
    }

    protected void clearItems() {
        dg_data.getDataProvider().getList().clear();
    }

    protected abstract void openEditor(T item, AsyncCallback<Void> saveCallback);

    protected abstract void deleteItem(Long id, AsyncCallback<Void> reloadCallback);

    private T getSelected() {
        return trySelectionModelValue(dg_data.getSelectionModel(), messages.noItemSelected(), itemsClass);
    }

    private void edit(T item) {
        openEditor(item, reloadDataCallback);
    }

}
