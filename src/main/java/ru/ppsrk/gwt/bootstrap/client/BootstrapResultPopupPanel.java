package ru.ppsrk.gwt.bootstrap.client;

import ru.ppsrk.gwt.client.ResultPopupPanel;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;

public abstract class BootstrapResultPopupPanel<T> extends ResultPopupPanel<T> {
    protected final HorizontalPanel horizontalPanel_buttons = new HorizontalPanel() {
    };
    private final Button button_ok = new Button();
    private final Button button_cancel = new Button();
    private final ButtonGroup buttonGroup = new ButtonGroup(button_ok, button_cancel);

    public BootstrapResultPopupPanel() {
        super();
        button_ok.addClickHandler(new Button_okClickHandler());
        button_ok.setIcon(IconType.OK);
        button_ok.setText("ОК");
        button_cancel.addClickHandler(new Button_cancelClickHandler());
        button_cancel.setIcon(IconType.REMOVE);
        button_cancel.setText("Отмена");
        horizontalPanel_buttons.add(buttonGroup);
    }

    protected boolean preventOk() {
        return false;
    }

    private class Button_okClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            if (!preventOk()) {
                hide();
            }
        }
    }

    private class Button_cancelClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            cancelHide();
        }
    }

    protected Button getOkButton() {
        return button_ok;
    }

    protected Button getCancelButton() {
        return button_cancel;
    }
}
