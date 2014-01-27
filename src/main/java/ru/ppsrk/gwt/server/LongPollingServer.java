package ru.ppsrk.gwt.server;

import java.util.concurrent.TimeUnit;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public abstract class LongPollingServer<T> {

    private long period;
    private long execDelay;

    public LongPollingServer(long period, long execDelay) {
        super();
        this.period = period;
        this.execDelay = execDelay;
    }

    public T start() throws InterruptedException, LogicException, ClientAuthException {
        long startTime = System.nanoTime();
        long nanoPeriod = TimeUnit.MILLISECONDS.toNanos(period);
        while (System.nanoTime() - startTime < nanoPeriod) {
            T result = exec();
            if (result != null) {
                return result;
            }
            Thread.sleep(execDelay);
        }
        return null;
    }

    public abstract T exec() throws LogicException, ClientAuthException, ClientAuthException;

}
