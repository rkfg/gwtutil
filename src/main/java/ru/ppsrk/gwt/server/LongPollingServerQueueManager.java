package ru.ppsrk.gwt.server;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.dto.LongPollingMessage;

public class LongPollingServerQueueManager<M extends LongPollingMessage> extends LongPollingServer<Collection<M>> {

    private ConcurrentLinkedQueue<M> queue = new ConcurrentLinkedQueue<>();

    private ThreadLocal<Long> lastTimestamp = new ThreadLocal<>();
    private ThreadLocal<Collection<M>> newMessages = ThreadLocal.withInitial(LinkedList::new);

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
    public void setTimestamp(Long fromTimestamp) {
        lastTimestamp.set(fromTimestamp);
    }

    @Override
    public Collection<M> exec() throws GwtUtilException {
        if (lastTimestamp.get() == null) {
            lastTimestamp.set(System.currentTimeMillis());
        }
        Collection<M> result = newMessages.get();
        if (!result.isEmpty()) {
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
        if (!result.isEmpty()) {
            return result;
        } else {
            return null; // null has a special meaning here so NOSONAR
        }
    }

    public void offer(M message) {
        queue.offer(message);
    }
}
