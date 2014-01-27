package ru.ppsrk.gwt.server;

import ru.ppsrk.gwt.client.LogicException;

public class LogicExceptionFormatted extends LogicException {

    /**
     * 
     */
    private static final long serialVersionUID = 7459072833147165310L;

    private LogicExceptionFormatted() {
        // can't be instantiated
    }

    public static LogicException format(String message, Object... args) {
        return new LogicException(String.format(message, args));
    }
}
