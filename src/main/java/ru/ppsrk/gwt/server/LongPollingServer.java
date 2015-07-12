package ru.ppsrk.gwt.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.client.LongPollingClient;
import ru.ppsrk.gwt.client.LongPollingClient.LongPollingAsyncCallback;

public abstract class LongPollingServer<T> implements AutoCloseable {

    protected long period;
    protected long execDelay;
    private Collection<Thread> workingThreads = new HashSet<Thread>();

    /**
     * Create a new long polling server
     * 
     * @param period
     *            how many ms to wait before returning with no result. Usually a
     *            relatively big number like 30000 for 30 seconds.
     * @param execDelay
     *            delay in ms between two subsequent polls. Represents
     *            granularity of the polling process, i.e. how fast you can
     *            react to events. Usually a relatively small number like 100
     *            ms, so the app responds to client in 100 ms at max after
     *            something changes. Too low delay may cause a high load.
     */

    public LongPollingServer(long period, long execDelay) {
        this.period = period;
        this.execDelay = execDelay;
    }

    protected void registerThread() {
        workingThreads.add(Thread.currentThread());
    }

    protected void deregisterThread() {
        workingThreads.remove(Thread.currentThread());
    }

    /**
     * Start the long polling operation. Call this from the GWT RPC handler that
     * the {@link LongPollingClient} calls in {@link LongPollingClient#doRPC(LongPollingAsyncCallback)}
     * 
     * @return
     * @throws InterruptedException
     * @throws LogicException
     * @throws ClientAuthException
     */

    public T start() throws InterruptedException, LogicException, ClientAuthException {
        try {
            registerThread();
            return awaitResult();
        } finally {
            deregisterThread();
        }
    }

    protected T awaitResult() throws LogicException, ClientAuthException, InterruptedException {
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

    @Override
    public void close() throws Exception {
        for (Thread thread : workingThreads) {
            thread.interrupt();
        }
    }

    /**
     * Your polling method which is called to check for any changes that should
     * be pushed to the client. Return null if there's nothing to send, in this
     * case the polling loop continues. Return non-null to immediately break the
     * polling loop and return the value to the client. After dispatching it the
     * client restarts the loop. Avoid querying the database, using I/O and
     * other slow operations in this method as it's usually called <b>10 and
     * more times per second per client</b> to provide responsive UX and nearly
     * realtime results.
     * 
     * @return
     * @throws LogicException
     * @throws ClientAuthException
     */
    public abstract T exec() throws LogicException, ClientAuthException;

}
