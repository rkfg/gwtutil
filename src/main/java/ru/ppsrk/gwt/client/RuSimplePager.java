package ru.ppsrk.gwt.client;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

public class RuSimplePager extends SimplePager {

    public RuSimplePager() {
        super(TextLocation.CENTER, false, true);
        getElement().getStyle().setProperty("display", "inline-table");
    }

    protected String createText() {
        // Default text is 1 based.
        NumberFormat formatter = NumberFormat.getFormat("####");
        HasRows display = getDisplay();
        Range range = display.getVisibleRange();
        int pageStart = range.getStart() + 1;
        int pageSize = range.getLength();
        int dataSize = display.getRowCount();
        int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
        endIndex = Math.max(pageStart, endIndex);
        boolean exact = display.isRowCountExact();
        return formatter.format(pageStart) + "-" + formatter.format(endIndex) + (exact ? " из " : " из более ")
                + formatter.format(dataSize);
    }
}