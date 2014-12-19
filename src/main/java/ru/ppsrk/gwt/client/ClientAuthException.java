package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClientAuthException extends Exception implements IsSerializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1581385418479089337L;

    /**
     * 
     */
    public ClientAuthException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public ClientAuthException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

}
