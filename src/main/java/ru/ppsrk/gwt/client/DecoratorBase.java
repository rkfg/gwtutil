package ru.ppsrk.gwt.client;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public abstract class DecoratorBase<T extends IsWidget> implements IsWidget, HasWidgets, HasVisibility {

    protected T decorated;

    @Override
    public void add(Widget w) {
        add((IsWidget) w);
    }

    @SuppressWarnings("unchecked")
    public void add(IsWidget w) {
        if (decorated != null) {
            throw new IllegalArgumentException("Only one element is allowed.");
        }
        checkType(w);
        decorated = (T) w;
    }

    protected abstract void checkType(IsWidget w);

    @Override
    public void clear() {
        decorated = null;
    }

    @Override
    public Iterator<Widget> iterator() {
        return new Iterator<Widget>() {

            boolean hasElement = decorated != null;

            Widget returned = null;

            public boolean hasNext() {
                return hasElement;
            }

            public Widget next() {
                if (!hasElement || (decorated == null)) {
                    throw new NoSuchElementException();
                }
                hasElement = false;
                return (returned = decorated.asWidget());
            }

            public void remove() {
                if (returned != null) {
                    DecoratorBase.this.remove(returned);
                }
            }
        };
    }

    @Override
    public boolean remove(Widget w) {
        boolean result = decorated != null;
        clear();
        return result;
    }

    @Override
    public Widget asWidget() {
        return decorated.asWidget();
    }

    @Override
    public boolean isVisible() {
        return decorated.asWidget().isVisible();
    }
    
    @Override
    public void setVisible(boolean visible) {
        decorated.asWidget().setVisible(visible);
    }
}
