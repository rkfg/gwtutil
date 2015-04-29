package ru.ppsrk.gwt.bootstrap.client;

import ru.ppsrk.gwt.bootstrap.client.ModalDialog.ModalDialogCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public abstract class ModalDialogAdapter<T> extends Composite {

    protected ModalDialog<T> modalDialog = new ModalDialog<T>() {
        @Override
        protected T getResult() {
            return ModalDialogAdapter.this.getResult();
        };
    };

    @Override
    protected void initWidget(Widget widget) {
        modalDialog.initForm(widget);
    }

    protected T getResult() {
        return null;
    }

    public void show() {
        modalDialog.show();
    }

    public ModalDialogAdapter<T> setDoneCallback(ModalDialogCallback<T> callback) {
        modalDialog.setDoneCallback(callback);
        return this;
    }

    public ModalDialogAdapter<T> enableOk(boolean enable) {
        modalDialog.b_ok.setEnabled(enable);
        return this;
    }

}
