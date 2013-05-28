package fr.mikrosimage.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ResizableDataGrid<T> extends DataGrid<T> {
    public interface GwtCssDataGridResources extends DataGrid.Resources {
        @Source({ Style.DEFAULT_CSS, "DataGrid.css" })
        Style dataGrid();
    }

    public static final GwtCssDataGridResources gwtCssDataGridResources = GWT.create(GwtCssDataGridResources.class);

    static {
        gwtCssDataGridResources.dataGrid().ensureInjected();
    }

    public class DataGridResizableHeader extends ResizableHeader<T> {
        public DataGridResizableHeader(String title, Column<T, ?> column) {
            super(title, ResizableDataGrid.this, column);
        }

        @Override
        protected int getTableBodyHeight() {
            return ResizableDataGrid.this.getTableBodyElement().getOffsetHeight();
        }

        /*
         * @Override public void columnResized(int newWidth) {
         * super.columnResized(newWidth); refreshColumnWidths(); }
         */

    }

    public ScrollPanel getScrollPanel() {
        final HeaderPanel headerPanel = (HeaderPanel) getWidget();
        return (ScrollPanel) headerPanel.getContentWidget();
    }

    @Override
    public void addColumn(Column<T, ?> col, String headerString) {
        super.addColumn(col, new DataGridResizableHeader(headerString, col));
    }

}
