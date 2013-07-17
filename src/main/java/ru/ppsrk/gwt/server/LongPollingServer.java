package ru.ppsrk.gwt.server;

import java.util.Date;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

public abstract class LongPollingServer<T> {

    private long period;
    private long execDelay;

    public LongPollingServer(long period, long execDelay) {
        super();
        this.period = period;
        this.execDelay = execDelay;
    }

    public T start() throws InterruptedException, LogicException, ClientAuthenticationException {
        long startTime = new Date().getTime();
        while (new Date().getTime() - startTime < period) {
            T result = exec();
            if (result != null) {
                return result;
            }
            Thread.sleep(execDelay);
        }
        return null;
    }

    public abstract T exec() throws LogicException, ClientAuthenticationException;

}
