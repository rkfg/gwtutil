package ru.ppsrk.gwt.bootstrap.client;

import static ru.ppsrk.gwt.client.ClientUtils.*;

import java.util.Collection;

import ru.ppsrk.gwt.client.ClientUtils.MyAsyncCallback;
import ru.ppsrk.gwt.client.event.ReloadDataEvent;
import ru.ppsrk.gwt.client.event.ReloadDataEvent.ReloadDataHandler;
import ru.ppsrk.gwt.client.HasListboxValue;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.SingleSelectionModel;

import fr.mikrosimage.gwt.client.CompleteResizableDataGrid;

public abstract class AbstractDataGridCRUD<T extends HasListboxValue> implements IsWidget {

    @UiField(provided = true)
    public CompleteResizableDataGrid<T, SingleSelectionModel<T>> dg_data = new CompleteResizableDataGrid<>(new SingleSelectionModel<T>());
    private EventBus eventBus;

    private CRUDMessages messages;
    private Class<T> itemsClass;

    public HandlerRegistration registerReload(EventBus eventBus, final Long groupId) {
        this.eventBus = eventBus;
        return eventBus.addHandler(ReloadDataEvent.TYPE, new ReloadDataHandler() {

            @Override
            public void onReloadData(ReloadDataEvent event) {
                if (event.groupId == null || event.groupId.equals(groupId)) {
                    loadData();
                }
            }
        });
    }

    public void broadcastReload(Long groupId) {
        eventBus.fireEvent(new ReloadDataEvent(groupId));
    }

    private MyAsyncCallback<Void> reloadDataCallback = new MyAsyncCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
            loadData(true);
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

    protected void loadData() {
        loadData(false);
    }

    protected abstract void getData(AsyncCallback<Collection<T>> dataCallback);

    protected void loadData(final boolean restorePosition) {
        dg_data.setLoadingData(null);
        getData(new MyAsyncCallback<Collection<T>>() {

            @Override
            public void onSuccess(Collection<T> result) {
                dg_data.setLoadingData(result, restorePosition);
                if (dg_data.getKeyboardSelectionPolicy() == KeyboardSelectionPolicy.BOUND_TO_SELECTION) {
                    dg_data.setKeyboardSelectedRow(0);
                }
            }
        });
    }

}
