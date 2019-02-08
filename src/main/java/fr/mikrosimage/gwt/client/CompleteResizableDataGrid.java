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

    /**
     * Add a column with a comparator.
     * 
     * @param col
     *            column to add
     * @param headerString
     *            column title
     * @param comparator
     *            comparator for values
     * @param sortAscending
     *            whether to sort this column ascending (true) or descending (false)
     * @param <C>
     *            column value type
     * @return col
     */

    public <C> Column<T, C> addColumn(Column<T, C> col, String headerString, Comparator<T> comparator, boolean sortAscending) {
        super.addColumn(col, new DataGridResizableHeader(headerString, col));
        col.setSortable(true);
        col.setDefaultSortAscending(sortAscending);
        sortHandler.setComparator(col, comparator);
        return col;
    }

    /**
     * Add a column with a comparator, sort ascending by default.
     * 
     * @param col
     *            column to add
     * @param headerString
     *            column title
     * @param comparator
     *            comparator for values
     * @param <C>
     *            column value type
     * @return col
     */
    public <C> Column<T, C> addColumn(Column<T, C> col, String headerString, Comparator<T> comparator) {
        return addColumn(col, headerString, comparator, true);
    }

    /**
     * Sets messages object for this data grid. It's used internally for representing null values in the columns as
     * {@link CommonMessages#notSet()}. Should be used before adding columns using the {@link #addDateColumn(Function, String)},
     * {@link #addLongColumn(Function, String)} etc.
     * 
     * @param messages
     */

    public void setMessages(CommonMessages messages) {
        this.messages = messages;
    }

    /**
     * Adds a column that represents a date. Also makes this column sortable using a field comparator.
     * 
     * <h3>Example:</h3>
     * 
     * <pre>
     * dg_data.addDateColumn(Entity::getDate, Main.messages.date());
     * </pre>
     * 
     * @param field
     *            function (usually a method reference) for extracting the field from the object
     * @param headerString
     *            column header text
     * @return the created column
     */
    public DateColumn<T> addDateColumn(Function<T, Date> field, String headerString) {
        return (DateColumn<T>) addColumn(new DateColumn<T>(messages) {

            @Override
            protected Date getDate(T object) {
                return field.apply(object);
            }
        }, headerString, new FieldComparator<>(field));
    }

    /**
     * Adds a column that represents a long number. Also makes this column sortable using a field comparator.
     * 
     * <h3>Example:</h3>
     * 
     * <pre>
     * dg_data.addLongColumn(Entity::getNumber, Main.messages.number());
     * </pre>
     * 
     * @param field
     *            function (usually a method reference) for extracting the field from the object.
     * @param headerString
     *            column header text
     * @return the created column
     */
    public LongColumn<T> addLongColumn(Function<T, Long> field, String headerString) {
        return (LongColumn<T>) addColumn(new LongColumn<T>(messages) {

            @Override
            protected Long getLongValue(T object) {
                return field.apply(object);
            }
        }, headerString, new FieldComparator<>(field));
    }

    /**
     * Adds a column that displays a string field from the object. Also makes this column sortable using a field comparator.
     * 
     * <h3>Example:</h3>
     * 
     * <pre>
     * dg_data.addTextColumn(Entity::getName, Main.messages.name());
     * </pre>
     * 
     * @param field
     *            function (usually a method reference) for extracting the field from the object.
     * @param headerString
     *            column header text
     * @return the created column
     */
    public TextColumn<T> addTextColumn(Function<T, String> field, String headerString) {
        return (TextColumn<T>) addColumn(new TextColumn<T>() {

            @Override
            public String getValue(T object) {
                return field.apply(object);
            }
        }, headerString, new FieldComparator<>(field));
    }

    /**
     * Adds a column that displays a string field from the object. Also makes this column sortable using a field comparator that uses a
     * specified field instead of the displayed one.
     * 
     * <h3>Example:</h3>
     * 
     * <pre>
     * dg_data.addTextColumn(Entity::getName, Entity::getNormalizedName, Main.messages.name());
     * </pre>
     * 
     * @param field
     *            function (usually a method reference) for extracting the field from the object.
     * @param compfield
     *            function (usually a method reference) for extracting the sorting field from the object.
     * @param headerString
     *            column header text
     * @return the created column
     */
    public <F extends Comparable<F>> TextColumn<T> addTextColumn(Function<T, String> field, Function<T, F> compfield, String headerString) {
        return (TextColumn<T>) addColumn(new TextColumn<T>() {

            @Override
            public String getValue(T object) {
                return field.apply(object);
            }
        }, headerString, new FieldComparator<>(compfield));
    }

    /**
     * Adds a column that displays a complex field of type {@link HasListboxValue} from the object. Also makes this column sortable using a
     * field comparator.
     * 
     * <h3>Example:</h3>
     * 
     * <pre>
     * dg_data.addHLBColumn(Entity::getType, Main.messages.type());
     * </pre>
     * 
     * @param field
     *            function (usually a method reference) for extracting the field from the object.
     * @param headerString
     *            column header text
     * @return the created column
     */
    public TextColumn<T> addHLBColumn(Function<T, HasListboxValue> field, String headerString) {
        Function<T, String> function = field.andThen(v -> v == null ? null : v.getListboxValue());
        return (TextColumn<T>) addColumn(new TextColumn<T>() {

            @Override
            public String getValue(T object) {
                HasListboxValue result = field.apply(object);
                if (result == null) {
                    return messages.notSet();
                }
                return result.getListboxValue();
            }
        }, headerString, new FieldComparator<>(function));
    }

    /**
     * Adds a column that displays a complex field of type {@link HasListboxValue} from the object. Also makes this column sortable using a
     * field comparator that uses a specified field instead of the displayed one.
     * 
     * <h3>Example:</h3>
     * 
     * <pre>
     * dg_data.addHLBColumn(Entity::getType, Entity::getComparableType, Main.messages.type());
     * </pre>
     * 
     * @param field
     *            function (usually a method reference) for extracting the field from the object.
     * @param headerString
     *            column header text
     * @return the created column
     */
    public <F extends Comparable<F>> TextColumn<T> addHLBColumn(Function<T, HasListboxValue> field, Function<T, F> compfield,
            String headerString) {
        return (TextColumn<T>) addColumn(new TextColumn<T>() {

            @Override
            public String getValue(T object) {
                HasListboxValue result = field.apply(object);
                if (result == null) {
                    return messages.notSet();
                }
                return result.getListboxValue();
            }
        }, headerString, new FieldComparator<>(compfield));
    }

    public void setDefaultSortColumn(Column<T, ?> col) {
        getColumnSortList().push(col);
    }
}
