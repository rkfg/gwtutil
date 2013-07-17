package ru.ppsrk.gwt.client;

public class PopupMenuItem {
    private String title;
    private String value;
    private boolean enabled = true;

    public PopupMenuItem(String title, String value) {
        super();
        this.title = title;
        this.value = value;
    }

    public PopupMenuItem(String title, String value, boolean enabled) {
        super();
        this.title = title;
        this.value = value;
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
