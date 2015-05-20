package ru.ppsrk.gwt.client;

import java.util.Collection;

import ru.ppsrk.gwt.dto.LongPollingMessage;

public abstract class LongPollingClientQueueManager<M extends LongPollingMessage> extends LongPollingClient<Collection<M>> {

    private Long lastTimestamp = null;

    /**
     * Create a new long polling queue manager.
     * 
     * @param failureDelay
     *            how many ms to wait if the long polling call was unsuccessful
     *            before restarting. Unsuccessful call means that an exception
     *            was returned, not just null because there's nothing to send.
     *            After receiving null the client restarts the polling
     *            immediately.
     */
    public LongPollingClientQueueManager(int failureDelay) {
        super(failureDelay);
    }

    /**
     * Returns the last known message timestamp or null if there's no prior
     * messages. Send this value to the server to get only actual messages.
     * 
     * @return last known message timestamp or null.
     */
    public Long getLastTimestamp() {
        return lastTimestamp;
    }

    /**
     * Call this from your implementation before processing to update the
     * lastTimestamp var.
     * 
     * @param result
     *            Received collection of messages
     */
    @Override
    public void success(Collection<M> result) {
        if (lastTimestamp == null && result.size() > 0) {
            lastTimestamp = result.iterator().next().getTimestamp();
        }
        for (M message : result) {
            Long msgTimestamp = message.getTimestamp();
            if (msgTimestamp != null && msgTimestamp > lastTimestamp) {
                lastTimestamp = msgTimestamp;
            }
        }
    }

}
