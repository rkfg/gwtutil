package fr.mikrosimage.gwt.client;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SetSelectionModel;

import ru.ppsrk.gwt.client.CommonMessages;
import ru.ppsrk.gwt.client.DateColumn;
import ru.ppsrk.gwt.client.HasId;
import ru.ppsrk.gwt.client.HasListboxValue;
import ru.ppsrk.gwt.client.LongColumn;
import ru.ppsrk.gwt.shared.FieldComparator;

public class CompleteResizableDataGrid<T extends HasId, S extends SetSelectionModel<T>> extends ResizableDataGrid<T> {
    private Set<Long> selectedSet = new HashSet<>();
    private S selectionModel;
    private ListDataProvider<T> dataProvider = new ListDataProvider<>();
    private ListHandler<T> sortHandler = new ListHandler<>(dataProvider.getList());
    private Range visibleRange = null;
    private int scrollPos = 0;
    private CommonMessages messages = null;

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

    public void resetLoadingData() {
        super.resetLoadingData(dataProvider);
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
                Scheduler.get().scheduleDeferred(() -> {
                    setVisibleRange(visibleRange);
                    getScrollPanel().setVerticalScrollPosition(scrollPos);
                    visibleRange = null;
                    scrollPos = 0;
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

    public <C> Column<T, C> addColumn(Column<T, C> col, String headerString, Comparator<T> comparator, boolean sortAscending) {
        super.addColumn(col, new DataGridResizableHeader(headerString, col));
        col.setSortable(true);
        col.setDefaultSortAscending(sortAscending);
        sortHandler.setComparator(col, comparator);
        return col;
    }

    public <C> Column<T, C> addColumn(Column<T, C> col, String headerString, Comparator<T> comparator) {
        return addColumn(col, headerString, comparator, true);
    }

    public void setMessages(CommonMessages messages) {
        this.messages = messages;
    }

    public DateColumn<T> addDateColumn(Function<T, Date> col, String headerString) {
        return (DateColumn<T>) addColumn(new DateColumn<T>(messages) {

            @Override
            protected Date getDate(T object) {
                return col.apply(object);
            }
        }, headerString, new FieldComparator<>(col));
    }

    public LongColumn<T> addLongColumn(Function<T, Long> col, String headerString) {
        return (LongColumn<T>) addColumn(new LongColumn<T>(messages) {

            @Override
            protected Long getLongValue(T object) {
                return col.apply(object);
            }
        }, headerString, new FieldComparator<>(col));
    }

    public TextColumn<T> addTextColumn(Function<T, String> col, String headerString) {
        return (TextColumn<T>) addColumn(new TextColumn<T>() {

            @Override
            public String getValue(T object) {
                return col.apply(object);
            }
        }, headerString, new FieldComparator<>(col));
    }

    public TextColumn<T> addHLBColumn(Function<T, HasListboxValue> col, String headerString) {
        Function<T, String> function = col.andThen(v -> v == null ? null : v.getListboxValue());
        return (TextColumn<T>) addColumn(new TextColumn<T>() {

            @Override
            public String getValue(T object) {
                HasListboxValue result = col.apply(object);
                if (result == null) {
                    return messages.notSet();
                }
                return result.getListboxValue();
            }
        }, headerString, new FieldComparator<>(function));
    }
}
