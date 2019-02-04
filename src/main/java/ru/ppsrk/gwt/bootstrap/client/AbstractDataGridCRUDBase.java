package ru.ppsrk.gwt.bootstrap.client;

import java.util.Collection;
import java.util.List;

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
import com.google.gwt.view.client.SetSelectionModel;

import fr.mikrosimage.gwt.client.CompleteResizableDataGrid;
import ru.ppsrk.gwt.client.ClientUtils.MyAsyncCallback;
import ru.ppsrk.gwt.client.HasListboxValue;
import ru.ppsrk.gwt.client.event.ReloadDataEvent;

public abstract class AbstractDataGridCRUDBase<T extends HasListboxValue, S extends SetSelectionModel<T>> implements IsWidget {

    @UiField(provided = true)
    public CompleteResizableDataGrid<T, S> dg_data;

    public AbstractDataGridCRUDBase(S selectionModel, Class<T> itemsClass, CRUDMessages messages) {
        dg_data = new CompleteResizableDataGrid<>(selectionModel);
        this.itemsClass = itemsClass;
        this.messages = messages;
    }

    private EventBus eventBus;

    protected CRUDMessages messages;
    protected Class<T> itemsClass;

    public HandlerRegistration registerReload(EventBus eventBus, final Long groupId) {
        this.eventBus = eventBus;
        return eventBus.addHandler(ReloadDataEvent.TYPE, event -> {
            if (event.groupId == null || event.groupId.equals(groupId)) {
                loadData();
            }
        });
    }

    public void broadcastReload(Long groupId) {
        eventBus.fireEvent(new ReloadDataEvent(groupId));
    }

    protected MyAsyncCallback<Void> reloadDataCallback = new MyAsyncCallback<Void>() {

        @Override
        public void onSuccess(Void result) {
            loadData(true);
        }
    };
    
    public class ListDataCallbackAdapter implements AsyncCallback<List<T>> {

        private AsyncCallback<Collection<T>> adaptee;

        public ListDataCallbackAdapter(AsyncCallback<Collection<T>> adaptee) {
            this.adaptee = adaptee;
        }
        
        @Override
        public void onFailure(Throwable caught) {
            adaptee.onFailure(caught);
        }

        @Override
        public void onSuccess(List<T> result) {
            adaptee.onSuccess(result);
        }
        
    }

    public interface CRUDMessages extends Messages {

        @DefaultMessage("No item selected")
        String noItemSelected();

        @DefaultMessage("Really delete the item {0}?")
        String confirmItemDeletion(String itemText);

    }

    protected void clearItems() {
        dg_data.getDataProvider().getList().clear();
    }

    protected abstract void openEditor(T item, AsyncCallback<Void> saveCallback);

    protected abstract void deleteItem(Long id, AsyncCallback<Void> reloadCallback);

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

    protected abstract void edit(T item);

    protected abstract T getSelected();

}
