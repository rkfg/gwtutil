package ru.ppsrk.gwt.client;

import ru.ppsrk.gwt.client.ClientUtils.LoadCellCallback;
import ru.ppsrk.gwt.client.TreeViewHelper.HasCellValue;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public abstract class TreeViewHelper<T extends HasCellValue> implements LoadCellCallback<T>, TreeViewModel {

    public interface HasCellValue extends SettableParent {
        String getCellValue();
    }

    protected SingleSelectionModel<T> selectionModel = new SingleSelectionModel<>();
    private ListDataProvider<T> rootDataProvider = new ListDataProvider<>();
    protected Cell<T> cell = new AbstractCell<T>() {

        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, HasCellValue value, SafeHtmlBuilder sb) {
            sb.appendEscaped(value.getCellValue());
        }
    };

    public TreeViewHelper(SingleSelectionModel<T> selectionModel) {
        this.selectionModel = selectionModel;
    }

    public TreeViewHelper() {
    }

    @SuppressWarnings("unchecked")
    public <E> NodeInfo<T> getNodeInfo(E value) {
        return new DefaultNodeInfo<T>(loadChildren((T) value), cell, selectionModel, null);
    }

    public SingleSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public ListDataProvider<T> loadChildren(T parent) {
        return ClientUtils.loadCellTree(parent, selectionModel, rootDataProvider, this);
    }

    @SuppressWarnings("unchecked")
    public ListDataProvider<T> loadSiblings(T sibling) {
        return loadChildren((T) sibling.getParent());
    }

    public ListDataProvider<T> getDataProvider() {
        return rootDataProvider;
    }

}
