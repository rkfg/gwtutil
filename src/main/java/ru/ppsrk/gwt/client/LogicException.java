package ru.ppsrk.gwt.client;

import java.io.Serializable;

public class LogicException extends Exception implements Serializable {

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
