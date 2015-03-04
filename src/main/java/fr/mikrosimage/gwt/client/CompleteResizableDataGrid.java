package fr.mikrosimage.gwt.client;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

public class CompleteResizableDataGrid<T, S extends SelectionModel<T>> extends ResizableDataGrid<T> {
    private S selectionModel;
    private ListDataProvider<T> dataProvider = new ListDataProvider<T>();
    private ListHandler<T> sortHandler = new ListHandler<T>(new ArrayList<T>());

    public CompleteResizableDataGrid(S selectionModel) {
        this.selectionModel = selectionModel;
        setup(dataProvider, selectionModel, null, sortHandler);
    }

    public S getSelectionModel() {
        return selectionModel;
    }

    public ListDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    public ListHandler<T> getSortHandler() {
        return sortHandler;
    }

    public void setLoadingData(Collection<? extends T> data) {
        super.setLoadingData(dataProvider, data);
        if (selectionModel instanceof SingleSelectionModel<?>) {
            ((SingleSelectionModel<?>) selectionModel).clear();
        } else if (selectionModel instanceof MultiSelectionModel<?>) {
            ((MultiSelectionModel<?>) selectionModel).clear();
        }
    }

}
