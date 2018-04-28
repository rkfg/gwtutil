package ru.ppsrk.gwt.dto;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LongPollingMessage implements Serializable {
    private long timestamp;

    public LongPollingMessage() {
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

}