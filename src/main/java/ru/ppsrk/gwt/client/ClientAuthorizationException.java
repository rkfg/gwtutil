package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClientAuthorizationException extends Exception implements IsSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2391183351233227736L;

    public ClientAuthorizationException() {
        super();
    }

    public ClientAuthorizationException(String message) {
        super(message);
    }
}
