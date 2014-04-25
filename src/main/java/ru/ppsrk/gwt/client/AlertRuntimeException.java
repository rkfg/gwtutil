package ru.ppsrk.gwt.client;

import java.io.Serializable;

public class AlertRuntimeException extends RuntimeException implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -529663235941113332L;

    /**
     * 
     */
    public AlertRuntimeException() {
        super();
    }

    /**
     * @param message
     */
    public AlertRuntimeException(String message) {
        super(message);
    }

}
