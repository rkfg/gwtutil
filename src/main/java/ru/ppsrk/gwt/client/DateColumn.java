package ru.ppsrk.gwt.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.TextColumn;

public abstract class DateColumn<T> extends TextColumn<T> {

    private DateTimeFormat format = DateTimeFormat.getFormat("dd-MM-yyyy");
    private CommonMessages messages;

    public DateColumn(CommonMessages messages) {
        this.messages = messages;
    }

    @Override
    public String getValue(T object) {
        Date date = getDate(object);
        if (date == null) {
            return messages.notSet();
        }
        return format.format(date);
    }

    protected abstract Date getDate(T object);

}
