package ru.ppsrk.gwt.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

import ru.ppsrk.gwt.client.ClientUtils.LoadCellCallback;
import ru.ppsrk.gwt.client.TreeViewHelper.HasCellValue;

public abstract class TreeViewHelper<T extends HasCellValue> implements LoadCellCallback<T>, TreeViewModel {

    public interface HasCellValue extends SettableParent {
        String getCellValue();

        default void render(Context context, HasCellValue value, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<span id='celltree" + value.getId() + "'>").appendEscaped(value.getCellValue())
                    .appendHtmlConstant("</span>");
        }
    }

    @SuppressWarnings("serial")
    public static class SeparatorCell implements HasCellValue {

        @Override
        public void setParent(Hierarchic parent) {

        }

        @Override
        public Hierarchic getParent() {
            return null;
        }

        @Override
        public Long getId() {
            return null;
        }

        @Override
        public void setId(Long newId) {

        }

        @Override
        public void render(Context context, HasCellValue value, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<hr style='border-top: 1px solid #999' />");
        }

        @Override
        public String getCellValue() {
            return null;
        }

    }

    protected SingleSelectionModel<T> selectionModel = new SingleSelectionModel<>();
    private ListDataProvider<T> rootDataProvider = new ListDataProvider<>();
    protected Cell<T> cell = new AbstractCell<T>() {

        @Override
        public void render(Context context, HasCellValue value, SafeHtmlBuilder sb) {
            value.render(context, value, sb);
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

    public void scrollSelectedIntoView(CellTree cellTree) {
        T selected = selectionModel.getSelectedObject();
        if (selected == null) {
            return;
        }
        scrollIntoView(cellTree, selected);
    }

    public void scrollIntoView(CellTree cellTree, T item) {
        NodeList<Element> elements = cellTree.getElement().getElementsByTagName("span");
        String id = "celltree" + item.getId();
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = elements.getItem(i);
            if (element.getId().equals(id)) {
                element.scrollIntoView();
                return;
            }
        }

    }

}
