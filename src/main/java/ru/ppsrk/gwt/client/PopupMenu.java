package ru.ppsrk.gwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PopupMenu extends PopupPanel implements HasSelectionHandlers<PopupMenuItem> {

    private VerticalPanel menuContentPanel = new VerticalPanel();
    private PopupMenuItem selectedItem = null;

    public PopupMenu() {
        super();
        setAutoHideEnabled(true);
        ScrollPanel scrollPanel = new ScrollPanel(menuContentPanel);
        scrollPanel.addStyleName("popup-menu");
        setWidget(scrollPanel);
    }

    public PopupMenu(String[][] items) {
        this();
        for (String[] item : items) {
            if (item.length < 1) {
                throw new AlertRuntimeException("Each item should contain 1 or 2 strings, this item has < 1 string.");
            }
            if (item.length > 2) {
                throw new AlertRuntimeException("Each item should contain 1 or 2 strings, this item has > 2 strings.");
            }
            if (item.length == 1) {
                addItem(new PopupMenuItem(item[0]));
            } else {
                addItem(new PopupMenuItem(item[0], item[1]));
            }
        }
    }

    public PopupMenu(String[][] items, SelectionHandler<PopupMenuItem> selectionHandler) {
        this(items);
        addSelectionHandler(selectionHandler);
    }

    public void addItem(final PopupMenuItem item) {
        if (item.isEnabled()) {
            item.addStyleName(PopupMenuItem.MENU_ITEM_ENABLED);
            item.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    hide();
                    SelectionEvent.fire(PopupMenu.this, item);
                    SelectionEvent.fire(item, item);
                }
            });
        } else {
            item.addStyleName(PopupMenuItem.MENU_ITEM_DISABLED);
        }
        item.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (item.isEnabled()) {
                    item.addStyleName(PopupMenuItem.MENU_ITEM_SELECTED);
                    selectedItem = item;
                }
            }
        });
        item.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                item.removeStyleName(PopupMenuItem.MENU_ITEM_SELECTED);
            }
        });
        menuContentPanel.add(item);
    }

    public HandlerRegistration addSelectionHandler(SelectionHandler<PopupMenuItem> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    public void moveSelection(int to) {
        int oldSelectedIndex;
        int newSelectedIndex;
        oldSelectedIndex = menuContentPanel.getWidgetIndex(selectedItem);
        newSelectedIndex = oldSelectedIndex + to;
        if (newSelectedIndex < 0) {
            newSelectedIndex = menuContentPanel.getWidgetCount() - 1;
        }
        if (newSelectedIndex >= menuContentPanel.getWidgetCount()) {
            newSelectedIndex = 0;
        }
        if (selectedItem != null) {
            selectedItem.removeStyleName(PopupMenuItem.MENU_ITEM_SELECTED);
        }
        selectedItem = (PopupMenuItem) menuContentPanel.getWidget(newSelectedIndex);
        selectedItem.addStyleName(PopupMenuItem.MENU_ITEM_SELECTED);
    }

    public void selectCurrentItem() {
        hide();
        SelectionEvent.fire(this, selectedItem);
    }

    @Override
    public void setPopupPositionAndShow(PositionCallback callback) {
        if (selectedItem != null) {
            selectedItem.removeStyleName(PopupMenuItem.MENU_ITEM_SELECTED);
            selectedItem = null;
        }
        super.setPopupPositionAndShow(callback);
    }
}
