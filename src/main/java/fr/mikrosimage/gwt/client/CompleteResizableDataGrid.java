package fr.mikrosimage.gwt.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ru.ppsrk.gwt.client.HasId;

import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SetSelectionModel;

public class CompleteResizableDataGrid<T extends HasId, S extends SetSelectionModel<T>> extends ResizableDataGrid<T> {
    private Set<Long> selectedSet = new HashSet<>();
    private S selectionModel;
    private ListDataProvider<T> dataProvider = new ListDataProvider<T>();
    private ListHandler<T> sortHandler = new ListHandler<T>(dataProvider.getList());

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
        if (data == null) {
            saveSelection();
            selectionModel.clear();
        }
        super.setLoadingData(dataProvider, data);
        if (data != null) {
            for (T item : data) {
                if (item.getId() != null && selectedSet.contains(item.getId())) {
                    selectionModel.setSelected(item, true);
                }
            }
        }
    }

    public void saveSelection() {
        selectedSet.clear();
        for (T item : selectionModel.getSelectedSet()) {
            selectedSet.add(item.getId());
        }
    }

    public void clearSelection() {
        selectionModel.clear();
        selectedSet.clear();
    }
}
