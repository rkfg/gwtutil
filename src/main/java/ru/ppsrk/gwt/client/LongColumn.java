package ru.ppsrk.gwt.client;

import com.google.gwt.user.cellview.client.TextColumn;

public abstract class LongColumn<T> extends TextColumn<T> {

    private CommonMessages messages;

    public LongColumn(CommonMessages messages) {
        this.messages = messages;
    }
    
    @Override
    public String getValue(T object) {
        Long longValue = getLongValue(object);
        if (longValue == null) {
            return messages.notSet();
        }
        return longValue.toString();
    }

    protected abstract Long getLongValue(T object);
}