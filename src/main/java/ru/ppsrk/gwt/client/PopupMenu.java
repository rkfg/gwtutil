package ru.ppsrk.gwt.client;

import java.util.HashMap;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PopupMenu extends PopupPanel implements HasSelectionHandlers<PopupMenuItem> {

    private VerticalPanel menuContentPanel = new VerticalPanel();
    private HashMap<Label, PopupMenuItem> menuItems = new HashMap<Label, PopupMenuItem>();
    private Label selectedItem = null;

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
                addItem(new PopupMenuItem(item[0], item[0]));
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
        final Label itemLabel = new Label(item.getTitle());
        menuItems.put(itemLabel, item);
        itemLabel.addStyleName("menu-item");
        if (item.isEnabled()) {
            itemLabel.addStyleName("menu-item-enabled");
            itemLabel.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    hide();
                    SelectionEvent.fire(PopupMenu.this, item);
                }
            });
        } else {
            itemLabel.addStyleName("menu-item-disabled");
        }
        itemLabel.addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (item.isEnabled()) {
                    itemLabel.addStyleName("menu-item-selected");
                    selectedItem = itemLabel;
                }
            }
        });
        itemLabel.addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                itemLabel.removeStyleName("menu-item-selected");
            }
        });
        menuContentPanel.add(itemLabel);
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
            selectedItem.removeStyleName("menu-item-selected");
        }
        selectedItem = (Label) menuContentPanel.getWidget(newSelectedIndex);
        selectedItem.addStyleName("menu-item-selected");
    }

    public void selectCurrentItem() {
        hide();
        SelectionEvent.fire(this, menuItems.get(selectedItem));
    }

    @Override
    public void setPopupPositionAndShow(PositionCallback callback) {
        if (selectedItem != null) {
            selectedItem.removeStyleName("menu-item-selected");
            selectedItem = null;
        }
        super.setPopupPositionAndShow(callback);
    }
}
