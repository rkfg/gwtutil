package ru.ppsrk.gwt.client;

public class HorizontalPanel extends com.google.gwt.user.client.ui.HorizontalPanel {

    private int padding;

    public void setPadding(int padding) {
        this.padding = padding;
        getTable().setPropertyInt("cellPadding", padding);
    }

    public int getPadding() {
        return padding;
    }
}
