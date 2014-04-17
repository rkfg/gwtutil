package ru.ppsrk.gwt.bootstrap.client;

import com.google.gwt.user.client.ui.Widget;

public interface KeyboardClosable {
    public void closeOk();

    public void closeCancel();

    public void setFocusedWidget(Widget widget);
}
