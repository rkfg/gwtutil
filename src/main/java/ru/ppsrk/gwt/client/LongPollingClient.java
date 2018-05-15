package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class LongPollingClient<T> {
    private int failureDelay;
    private LongPollingAsyncCallback asyncCallback = new LongPollingAsyncCallback();

    public class LongPollingAsyncCallback implements AsyncCallback<T> {

        @Override
        public void onFailure(final Throwable caught) {
            new Timer() {

                @Override
                public void run() {
                    start();
                }
            }.schedule(failureDelay);
            failure(caught);
        }

        @Override
        public void onSuccess(T result) {
            try {
                if (result != null) {
                    success(result);
                } else {
                    nothing();
                }
            } finally {
                start();
            }
        }
    }

    /**
     * Create a new long polling client.
     * 
     * @param failureDelay
     *            how many ms to wait if the long polling call was unsuccessful
     *            before restarting. Unsuccessful call means that an exception
     *            was returned, not just null because there's nothing to send.
     *            After receiving null the client restarts the polling
     *            immediately.
     */
    public LongPollingClient(int failureDelay) {
        this.failureDelay = failureDelay;
    }

    public void start() {
        doRPC(asyncCallback);
    }

    /**
     * Do your RPC call to the server-side and supply this callback.
     * 
     * @param callback
     *            internal callback which calls your callback and restarts the
     *            long polling
     */
    public abstract void doRPC(LongPollingAsyncCallback callback);

    /**
     * This is executed on successful retrieval of non-null data from the poll
     * 
     * @param result
     */
    public abstract void success(T result);

    public void failure(Throwable caught) {
        // do nothing by default
    }

    public void nothing() {
        // do nothing when no updates came
    }
}
