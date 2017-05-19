package ru.ppsrk.gwt.client;

public class LogicException extends GwtUtilException {

    private static final long serialVersionUID = 6882692375946208921L;

    public LogicException(String message) {
        super(message);
    }
    
    public LogicException(String message, Throwable e) {
        super(message, e);
    }

    public LogicException() {
        super();
    }

}
