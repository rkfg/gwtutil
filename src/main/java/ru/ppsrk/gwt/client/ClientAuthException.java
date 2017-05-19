package ru.ppsrk.gwt.client;

import java.io.Serializable;

public class ClientAuthException extends GwtUtilException implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1581385418479089337L;

    /**
     * 
     */
    public ClientAuthException() {
    }

    /**
     * @param message
     */
    public ClientAuthException(String message) {
        super(message);
    }

}
