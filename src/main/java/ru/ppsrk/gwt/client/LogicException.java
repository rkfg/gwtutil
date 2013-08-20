package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LogicException extends Exception implements IsSerializable {

    public LogicException(String string) {
        super(string);
    }

    public LogicException() {
        super();
    }

    /**
     * 
     */
    private static final long serialVersionUID = 6882692375946208921L;

}
