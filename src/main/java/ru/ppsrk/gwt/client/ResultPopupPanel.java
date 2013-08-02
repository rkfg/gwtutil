package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.PopupPanel;

public abstract class ResultPopupPanel<T> extends PopupPanel {

    public static interface ResultPopupPanelCallback<T> {
        public void done(T result);
    }

    private ResultPopupPanelCallback<T> callback;

    public void setResultCallback(ResultPopupPanelCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void hide() {
        callback.done(getResult());
        super.hide();
    }

    protected void cancelHide() {
        super.hide();
    }

    protected abstract T getResult();

    public abstract FocusWidget getFocusWidget();
}
