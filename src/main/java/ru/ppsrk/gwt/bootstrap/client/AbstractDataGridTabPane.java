package ru.ppsrk.gwt.bootstrap.client;

import java.util.Collection;

import ru.ppsrk.gwt.client.ClientUtils.MyAsyncCallback;
import ru.ppsrk.gwt.client.HasId;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SingleSelectionModel;

import fr.mikrosimage.gwt.client.CompleteResizableDataGrid;

public abstract class AbstractDataGridTabPane<T extends HasId> extends TabPaneFixed {

    @UiField(provided = true)
    public CompleteResizableDataGrid<T, SingleSelectionModel<T>> dg_data = new CompleteResizableDataGrid<>(new SingleSelectionModel<T>());

    protected void loadData() {
        dg_data.setLoadingData(null);
        getData(new MyAsyncCallback<Collection<T>>() {

            @Override
            public void onSuccess(Collection<T> result) {
                dg_data.setLoadingData(result);
            }
        });
    }

    protected abstract void getData(AsyncCallback<Collection<T>> dataCallback);

    @Override
    protected void onShown() {
        dg_data.redraw();
    }

}
