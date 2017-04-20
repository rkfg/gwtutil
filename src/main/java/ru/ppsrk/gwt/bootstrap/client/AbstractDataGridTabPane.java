package ru.ppsrk.gwt.bootstrap.client;

import java.util.Collection;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SingleSelectionModel;

import fr.mikrosimage.gwt.client.CompleteResizableDataGrid;
import ru.ppsrk.gwt.client.ClientUtils.MyAsyncCallback;
import ru.ppsrk.gwt.client.event.ReloadDataEvent;
import ru.ppsrk.gwt.client.event.ReloadDataEvent.ReloadDataHandler;
import ru.ppsrk.gwt.client.HasId;

public abstract class AbstractDataGridTabPane<T extends HasId> extends TabPaneFixed {

    @UiField(provided = true)
    public CompleteResizableDataGrid<T, SingleSelectionModel<T>> dg_data = new CompleteResizableDataGrid<>(new SingleSelectionModel<T>());
    private EventBus eventBus;

    protected void loadData() {
        loadData(false);
    }
    
    protected void loadData(final boolean restorePosition) {
        dg_data.setLoadingData(null);
        getData(new MyAsyncCallback<Collection<T>>() {

            @Override
            public void onSuccess(Collection<T> result) {
                dg_data.setLoadingData(result, restorePosition);
            }
        });
    }

    protected abstract void getData(AsyncCallback<Collection<T>> dataCallback);

    @Override
    protected void onShown() {
        dg_data.redraw();
    }

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

}
