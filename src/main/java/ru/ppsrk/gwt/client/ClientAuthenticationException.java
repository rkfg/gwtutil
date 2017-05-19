package ru.ppsrk.gwt.client;

import java.io.Serializable;

public class ClientAuthenticationException extends ClientAuthException implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2566225158467251180L;

    public ClientAuthenticationException() {
    }

    public ClientAuthenticationException(String message) {
        super(message);
    }
}
