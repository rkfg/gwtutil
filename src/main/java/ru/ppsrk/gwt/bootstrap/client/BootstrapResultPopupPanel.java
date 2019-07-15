package ru.ppsrk.gwt.bootstrap.client;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ButtonGroup;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import ru.ppsrk.gwt.client.ResultPopupPanel;

public abstract class BootstrapResultPopupPanel<T> extends ResultPopupPanel<T> implements KeyboardClosable {
    protected final HorizontalPanel horizontalPanel_buttons = new HorizontalPanel() {
    };
    private final Button button_ok = new Button();
    private final Button button_cancel = new Button();
    private final ButtonGroup buttonGroup = new ButtonGroup(button_ok, button_cancel);
    private Widget focusedWidget = null;
    private boolean isKeyboardClosable = false;

    public BootstrapResultPopupPanel() {
        this(true);
    }

    public BootstrapResultPopupPanel(boolean keyboardClosable) {
        super();
        button_ok.addClickHandler(e -> {
            if (!preventOk()) {
                hide();
            }
        });
        button_ok.setIcon(IconType.OK);
        button_ok.setText("ОК");
        button_cancel.addClickHandler(e -> cancelHide());
        button_cancel.setIcon(IconType.REMOVE);
        button_cancel.setText("Отмена");
        horizontalPanel_buttons.add(buttonGroup);
        isKeyboardClosable = keyboardClosable;
    }

    protected boolean preventOk() {
        return false;
    }

    protected Button getOkButton() {
        return button_ok;
    }

    protected Button getCancelButton() {
        return button_cancel;
    }

    protected void placeButtons(FlexTable flexTable, int row, int col, int colspan) {
        placeButtons(flexTable, row, col, colspan, true);
    }

    protected void placeButtons(FlexTable flexTable, int row, int col, int colspan, boolean center) {
        flexTable.setWidget(row, col, horizontalPanel_buttons);
        flexTable.getFlexCellFormatter().setColSpan(row, col, colspan);
        if (center) {
            flexTable.getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_CENTER);
        }
    }

    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        if (event.getNativeEvent().getType().equals("keydown") && event.isFirstHandler()) {
            switch (event.getNativeEvent().getKeyCode()) {
            case KeyCodes.KEY_ESCAPE:
                if (!(getFocusedWidget() instanceof PreventsKeyboardClose)) {
                    event.consume();
                    closeCancel();
                }
                break;
            case KeyCodes.KEY_ENTER:
                // typeahead box hides this panel automatically if possible
                if (!(getFocusedWidget() instanceof PreventsKeyboardClose)) {
                    event.consume();
                    closeOk();
                }
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void closeOk() {
        if (isKeyboardClosable) {
            button_ok.click();
        }
    }

    @Override
    public void closeCancel() {
        if (isKeyboardClosable) {
            button_cancel.click();
        }
    }

    private Widget getFocusedWidget() {
        return focusedWidget;
    }

    @Override
    public void setFocusedWidget(Widget widget) {
        focusedWidget = widget;
    }

    public boolean isKeyboardClosable() {
        return isKeyboardClosable;
    }

}
