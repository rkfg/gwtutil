package ru.ppsrk.gwt.client;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

import ru.ppsrk.gwt.dto.LongPollingMessage;

public abstract class LongPollingClientQueueManager<M extends LongPollingMessage> extends LongPollingClient<Collection<M>> {

    private Long lastTimestamp = 0L;

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
        Stream.concat(result.stream().map(M::getTimestamp), Stream.of(lastTimestamp)).max(Comparator.naturalOrder())
                .ifPresent(v -> lastTimestamp = v);
    }

}
