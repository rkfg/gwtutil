package ru.ppsrk.gwt.client;

public class VerticalPanel extends com.google.gwt.user.client.ui.VerticalPanel {

    private int padding;

    public void setPadding(int padding) {
        this.padding = padding;
        getTable().setPropertyInt("cellPadding", padding);
    }

    public int getPadding() {
        return padding;
    }
}
