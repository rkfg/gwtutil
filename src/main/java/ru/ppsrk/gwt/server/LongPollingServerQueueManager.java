package ru.ppsrk.gwt.server;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.dto.LongPollingMessage;

public abstract class LongPollingServerQueueManager<M extends LongPollingMessage> extends LongPollingServer<Collection<M>> {

    private ConcurrentLinkedQueue<M> queue = new ConcurrentLinkedQueue<>();

    private ThreadLocal<Long> lastTimestamp = new ThreadLocal<>();
    private ThreadLocal<Collection<M>> newMessages = new ThreadLocal<Collection<M>>() {
        @Override
        protected Collection<M> initialValue() {
            return new LinkedList<M>();
        }

    };

    private long messagesTimeout;

    /**
     * Create a new long polling queue manager
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
     * @param messagesTimeout
     *            period in ms after which a message expires and is removed from
     *            the queue. Used to avoid race conditions with newly connected
     *            clients that may miss some messages while receiving the
     *            initial app state. Depending on the message rate you may
     *            prefer it to be large or small.
     */

    public LongPollingServerQueueManager(long period, long execDelay, long messagesTimeout) {
        super(period, execDelay);
        this.messagesTimeout = messagesTimeout;
    }

    /**
     * Set the timestamp. Call this before starting the server.
     * 
     * @param fromTimestamp
     *            client-supplied timestamp value, messages received after it will be sent back.
     */
    public void setTimestamp(long fromTimestamp) {
        lastTimestamp.set(fromTimestamp);
    }

    @Override
    public Collection<M> exec() throws LogicException, ClientAuthException, ClientAuthException {
        if (lastTimestamp.get() == null) {
            lastTimestamp.set(System.currentTimeMillis());
        }
        Collection<M> result = newMessages.get();
        if (result.size() > 0) {
            result.clear();
        }
        long maxTimestamp = lastTimestamp.get();
        for (M message : queue) {
            if (System.currentTimeMillis() - message.getTimestamp() > messagesTimeout) {
                queue.remove(message);
            } else if (message.getTimestamp() > lastTimestamp.get()) {
                result.add(message);
                if (message.getTimestamp() > maxTimestamp) {
                    maxTimestamp = message.getTimestamp();
                }
            }
        }
        lastTimestamp.set(maxTimestamp);
        if (result.size() > 0) {
            return result;
        } else {
            return null;
        }
    }

    public void offer(M message) {
        queue.offer(message);
    }
}
