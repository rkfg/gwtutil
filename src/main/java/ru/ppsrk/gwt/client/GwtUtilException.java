package ru.ppsrk.gwt.client;

import java.io.Serializable;

public class GwtUtilException extends Exception implements Serializable {

    private static final long serialVersionUID = -4483557957304713754L;

    public GwtUtilException() {
    }

    public GwtUtilException(String message) {
        super(message);
    }

    public GwtUtilException(String message, Throwable e) {
        super(message, e);
    }

}
