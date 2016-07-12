package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.ui.HasEnabled;

import ru.ppsrk.gwt.client.ClientUtils.MyAsyncCallback;

public abstract class PreventingCallback<T> extends MyAsyncCallback<T> {

    private HasEnabled widget;

    public PreventingCallback(HasEnabled widget) {
        this.widget = widget;
        widget.setEnabled(false);
    }

    @Override
    public void onSuccess(T result) {
        widget.setEnabled(true);
        doSuccess(result);
    }

    protected abstract void doSuccess(T result);

    @Override
    public void onFailure(Throwable caught) {
        widget.setEnabled(true);
        super.onFailure(caught);
    }

}
