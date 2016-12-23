package ru.ppsrk.gwt.bootstrap.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

import ru.ppsrk.gwt.bootstrap.client.ModalDialog.ModalDialogCallback;
import ru.ppsrk.gwt.client.PreventingCallback;

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

    public class PreventingModalDialogCallback<MT> extends PreventingCallback<MT> {

        public PreventingModalDialogCallback() {
            super(modalDialog.b_ok);
        }

        @Override
        protected void doSuccess(MT result) {
            modalDialog.doOk();
        }

    }

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
        modalDialog.m_dialog.setMaxHeigth(maxHeight);
    }

    public void setWidth(int width) {
        modalDialog.m_dialog.setWidth(width);
    }

    /**
     * Set to false if typeahead's dropdown isn't fully visible
     * 
     * @param enabled
     *            if true, apply the default behavior (show scrollbar on
     *            overflow), if false always show the overflowing content
     */
    protected void setOverflowEnabled(boolean enabled) {
        Element child = modalDialog.m_dialog.getElement().getFirstChildElement();
        while (child != null) {
            if (child.getClassName().equals("modal-body")) {
                child.getStyle().setOverflowY(enabled ? Overflow.AUTO : Overflow.VISIBLE);
                break;
            }
            child = child.getNextSiblingElement();
        }
    }

}
