package ru.ppsrk.gwt.client;

import java.io.Serializable;

public class ClientAuthorizationException extends ClientAuthException implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2391183351233227736L;

    public ClientAuthorizationException() {
    }

    public ClientAuthorizationException(String message) {
        super(message);
    }

}
