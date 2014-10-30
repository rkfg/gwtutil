package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.DialogBox;

public abstract class ResultPopupPanel<T> extends DialogBox {

    public static interface ResultPopupPanelCallback<T> {
        public void done(T result);
    }

    public ResultPopupPanel() {
        getElement().getStyle().setZIndex(10);
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

    public abstract FocusWidget getFocusWidget();

    public void open(ResultPopupPanelCallback<T> callback) {
        ClientUtils.openPopupPanel(this, callback);
    }

}
