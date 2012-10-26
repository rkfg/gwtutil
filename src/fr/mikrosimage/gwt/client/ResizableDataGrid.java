package fr.mikrosimage.gwt.client;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ResizableDataGrid<T> extends DataGrid<T> {
    public class DataGridResizableHeader extends ResizableHeader<T> {
        public DataGridResizableHeader(String title, Column<T, ?> column) {
            super(title, ResizableDataGrid.this, column);
        }

        @Override
        protected int getTableBodyHeight() {
            return ResizableDataGrid.this.getTableBodyElement().getOffsetHeight();
        }

        @Override
        public void columnResized(int newWidth) {
            super.columnResized(newWidth);
            refreshColumnWidths();
        }

    }
    public ScrollPanel getScrollPanel() {
        final HeaderPanel headerPanel = (HeaderPanel) getWidget();
        return (ScrollPanel) headerPanel.getContentWidget();
    }
}
