package ru.ppsrk.gwt.bootstrap.client;

import ru.ppsrk.gwt.bootstrap.client.ModalDialog.ModalDialogCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

public abstract class ModalDialogAdapter<T> extends Composite {

    private int deferredCounter;

    protected ModalDialog<T> modalDialog = new ModalDialog<T>() {
        @Override
        protected T getResult() {
            return ModalDialogAdapter.this.getResult();
        };

        protected boolean preventOk() {
            return ModalDialogAdapter.this.preventOk();
        };
    };

    @Override
    protected void initWidget(Widget widget) {
        modalDialog.initForm(widget);
    }

    protected void setDeferredCount(int cnt) {
        deferredCounter = cnt;
        if (cnt > 0) {
            enableOk(false);
        }
    }

    protected void loaded() {
        if (--deferredCounter == 0) {
            enableOk(true);
        }
    }

    protected boolean preventOk() {
        return false;
    }

    protected void setFocusable(Focusable widget) {
        modalDialog.setFocusable(widget);
    }

    protected abstract T getResult();

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

    public void setMaxHeight(String maxHeight) {
        modalDialog.setMaxHeight(maxHeight);
    }

    public void setWidth(int width) {
        modalDialog.setWidth(width);
    }

}
