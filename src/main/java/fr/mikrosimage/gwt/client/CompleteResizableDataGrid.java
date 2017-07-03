package fr.mikrosimage.gwt.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SetSelectionModel;

import ru.ppsrk.gwt.client.HasId;

public class CompleteResizableDataGrid<T extends HasId, S extends SetSelectionModel<T>> extends ResizableDataGrid<T> {
    private Set<Long> selectedSet = new HashSet<>();
    private S selectionModel;
    private ListDataProvider<T> dataProvider = new ListDataProvider<>();
    private ListHandler<T> sortHandler = new ListHandler<>(dataProvider.getList());
    private Range visibleRange = null;
    private int scrollPos = 0;

    public CompleteResizableDataGrid(S selectionModel) {
        this.selectionModel = selectionModel;
        setup(dataProvider, selectionModel, null, sortHandler);
    }

    @Override
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
        setLoadingData(data, false);
    }
    
    public void setLoadingData(Collection<? extends T> data, boolean restorePosition) {
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
            if (restorePosition && visibleRange != null) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        setVisibleRange(visibleRange);
                        getScrollPanel().setVerticalScrollPosition(scrollPos);
                        visibleRange = null;
                        scrollPos = 0;
                    }
                });
            }
        }
    }

    public void saveSelection() {
        visibleRange = getVisibleRange();
        scrollPos = getScrollPanel().getVerticalScrollPosition();
        selectedSet.clear();
        for (T item : selectionModel.getSelectedSet()) {
            selectedSet.add(item.getId());
        }
    }

    public void clearSelection() {
        selectionModel.clear();
        selectedSet.clear();
        visibleRange = null;
    }
}
