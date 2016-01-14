package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Form;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.event.HiddenEvent;
import com.github.gwtbootstrap.client.ui.event.HiddenHandler;
import com.github.gwtbootstrap.client.ui.event.ShownEvent;
import com.github.gwtbootstrap.client.ui.event.ShownHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

public class ModalDialog<T> extends Composite {

    public interface ModalDialogCallback<T> {
        public void done(T result);
    }

    private static ModalDialogUiBinder uiBinder = GWT.create(ModalDialogUiBinder.class);

    interface ModalDialogUiBinder extends UiBinder<Widget, ModalDialog<?>> {
    }

    @UiField
    Modal m_editor;

    @UiField
    Form f_main;

    @UiField
    Button b_ok;

    private ModalDialog<?> parent = null;

    private Focusable focusable;

    private ModalDialogCallback<T> callback;

    public HandlerRegistration addHiddenHandler(HiddenHandler handler) {
        return m_editor.addHiddenHandler(handler);
    }

    public HandlerRegistration addShownHandler(ShownHandler handler) {
        return m_editor.addShownHandler(handler);
    }

    public ModalDialog() {
        uiBinder.createAndBindUi(this);
        m_editor.addShownHandler(new ShownHandler() {

            @Override
            public void onShown(ShownEvent shownEvent) {
                if (focusable != null) {
                    focusable.setFocus(true);
                }
            }
        });
        m_editor.addHiddenHandler(new HiddenHandler() {
            
            @Override
            public void onHidden(HiddenEvent hiddenEvent) {
                showParent();
            }
        });
    }

    public void show() {
        m_editor.show();
    }

    @UiHandler("b_ok")
    public void onOkClick(ClickEvent e) {
        if (preventOk()) {
            return;
        }
        doOk();
    }

    public void doOk() {
        if (callback != null) {
            callback.done(getResult());
        }
        m_editor.hide();
    }

    protected boolean preventOk() {
        return false;
    }

    protected T getResult() {
        return null;
    }

    @UiHandler("b_cancel")
    public void onCancelClick(ClickEvent e) {
        m_editor.hide();
    }

    private void showParent() {
        if (parent != null) {
            parent.show();
        }
    }

    @UiHandler("sb_ok")
    public void onSubmitClick(ClickEvent e) {
        onOkClick(e);
    }

    public void initForm(Widget formContent) {
        f_main.add(formContent);
    }

    public void enableOk(boolean enabled) {
        b_ok.setEnabled(enabled);
    }

    public void setParent(ModalDialog<?> parent) {
        this.parent = parent;
    }

    public void setFocusable(Focusable focusable) {
        this.focusable = focusable;
    }

    public void setMaxHeight(String maxHeight) {
        m_editor.setMaxHeigth(maxHeight);
    }

    public void setDoneCallback(ModalDialogCallback<T> callback) {
        this.callback = callback;
    }

    public void setWidth(int width) {
        m_editor.setWidth(width);
    }

    public void hide() {
        m_editor.hide();
    }

    public void addStyleName(String style) {
        m_editor.addStyleName(style);
    }

    public void removeStyleName(String style) {
        m_editor.removeStyleName(style);
    }

}
