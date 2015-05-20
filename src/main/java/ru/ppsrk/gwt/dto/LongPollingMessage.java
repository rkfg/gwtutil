package ru.ppsrk.gwt.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LongPollingMessage implements IsSerializable {
    private long timestamp;

    public LongPollingMessage() {
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

}