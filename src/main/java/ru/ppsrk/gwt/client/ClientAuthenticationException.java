package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClientAuthenticationException extends Exception implements IsSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2566225158467251180L;

    public ClientAuthenticationException() {
        super();
    }

    public ClientAuthenticationException(String message) {
        super(message);
    }
}
