package ru.ppsrk.gwt.client;

import java.util.List;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import gwtquery.plugins.droppable.client.gwt.DragAndDropCellList;

public class ListFilter<T extends HasListboxValue> extends Composite {

    public int minCharCount = 2;
    private ListFilterCallback<T> callback = null;

    public void setCallback(ListFilterCallback<T> callback) {
        this.callback = callback;
    }

    private final ListDataProvider<T> dataProvider_elements = new ListDataProvider<T>();
    private final SingleSelectionModel<T> selectionModel_elements = new SingleSelectionModel<T>();

    public interface ListFilterCallback<T extends HasListboxValue> {
        public void loadAll(AsyncCallback<List<T>> callback);

        public void loadFiltered(String filter, AsyncCallback<List<T>> callback);
    }

    private AsyncCallback<List<T>> elementsLoadCallback = new ClientUtils.MyAsyncCallback<List<T>>() {
        @Override
        public void onSuccess(List<T> result) {
            dataProvider_elements.getList().clear();
            dataProvider_elements.getList().addAll(result);
        }
    };

    private static ListFilterUiBinder uiBinder = GWT.create(ListFilterUiBinder.class);

    interface ListFilterUiBinder extends UiBinder<Widget, ListFilter<?>> {
    }

    @UiField(provided = true)
    CellList<T> cl_items = new DragAndDropCellList<>(new AbstractCell<T>() {

        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, T value, SafeHtmlBuilder sb) {
            sb.appendEscaped(value.getListboxValue());
        }
    });

    @UiField
    TextBox tb_filter;

    @UiField
    ScrollPanel sp_items;

    public ListFilter() {
        initWidget(uiBinder.createAndBindUi(this));
        dataProvider_elements.addDataDisplay(cl_items);
        cl_items.setSelectionModel(selectionModel_elements);
    }

    public void loadElements(boolean loadAll) {
        if (loadAll) {
            tb_filter.setValue("");
            if (callback != null) {
                callback.loadAll(elementsLoadCallback);
            }
        } else {
            if (callback != null) {
                callback.loadFiltered(tb_filter.getValue(), elementsLoadCallback);
            }
        }
    }

    public void setFocus() {
        tb_filter.setFocus(true);
    }

    public void addElement(T elementDTO) {
        dataProvider_elements.getList().add(elementDTO);
    }

    public SingleSelectionModel<T> getSelectionModel() {
        return selectionModel_elements;
    }

    @UiHandler("b_clear")
    public void onClearClick(ClickEvent event) {
        loadElements(true);
        tb_filter.setFocus(true);
    }

    @UiHandler("tb_filter")
    public void onKeyUp(KeyUpEvent event) {
        if (!tb_filter.getValue().isEmpty()) {
            if (tb_filter.getValue().length() > minCharCount) {
                loadElements(false);
            }
        } else {
            loadElements(true);
        }
    }

    public HandlerRegistration addSelectionChangeHandler(Handler handler) {
        return selectionModel_elements.addSelectionChangeHandler(handler);
    }

    public void update() {
        loadElements(tb_filter.getValue().isEmpty());
    }

    public void setMinCharCount(int charCount) {
        minCharCount = charCount;
    }

    @Override
    public void setHeight(String height) {
        sp_items.setHeight(height);
    }

}
