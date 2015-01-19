package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Focusable;

public abstract class ResultPopupPanel<T> extends DialogBox {

    public static interface ResultPopupPanelCallback<T> {
        public void done(T result);
    }

    private ResultPopupPanelCallback<T> callback;

    public void setResultCallback(ResultPopupPanelCallback<T> callback) {
        this.callback = callback;
    }

    public void done() {
        if (callback != null) {
            callback.done(getResult());
        }
    }

    @Override
    public void hide() {
        done();
        super.hide();
    }

    public void cancelHide() {
        super.hide();
    }

    protected abstract T getResult();

    public abstract Focusable getFocusable();

    public void open(ResultPopupPanelCallback<T> callback) {
        ClientUtils.openPopupPanel(this, callback);
    }

}
