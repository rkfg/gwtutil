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
                }
            } finally {
                start();
            }
        }
    }

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

}
