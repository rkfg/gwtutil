package ru.ppsrk.gwt.server;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ru.ppsrk.gwt.client.AlertRuntimeException;

@SuppressWarnings("serial")
public abstract class AnnotatedServlet extends RemoteServiceServlet {

    public interface IAnnotationProcessor {
        public void process(Method implMethod) throws Throwable;
    }

    public interface IRPCExceptionHandler {
        /**
         * Handle the exception
         * 
         * @param e
         *            throwable
         * @return serialized exception if handled successfully, null otherwise
         */
        public String handle(Method method, RPCRequest rpcRequest, Throwable e);
    }

    public interface IRPCFinalizer {
        public void cleanup();
    }

    private Deque<IAnnotationProcessor> checkers = new LinkedList<IAnnotationProcessor>();
    private Deque<IRPCExceptionHandler> handlers = new LinkedList<IRPCExceptionHandler>();
    private Deque<IRPCFinalizer> finalizers = new LinkedList<IRPCFinalizer>();

    public class AlertHandler implements IRPCExceptionHandler {

        @Override
        public String handle(Method method, RPCRequest rpcRequest, Throwable e) {
            try {
                if (e instanceof NoSuchMethodException) {
                    return RPC.encodeResponseForFailure(method, new AlertRuntimeException("method not found: " + method),
                            rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
                }
                if (e instanceof SecurityException) {
                    return RPC.encodeResponseForFailure(method,
                            new AlertRuntimeException("security exception on method resolving: " + method),
                            rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());

                }
            } catch (SerializationException e1) {
            }
            return null;
        }

    }

    protected AnnotatedServlet() {
        addExceptionHandler(new AlertHandler(), false);
    }

    @Override
    public String processCall(String payload) throws SerializationException {
        RPCRequest rpcRequest = RPC.decodeRequest(payload, getClass(), this);
        Method method = rpcRequest.getMethod();
        String result = null;
        try {
            Method implMethod = this.getClass().getMethod(method.getName(), method.getParameterTypes());
            AuthServiceImpl.setMDCIP(getThreadLocalRequest());
            for (IAnnotationProcessor checker : checkers) {
                checker.process(implMethod);
            }
            result = super.processCall(payload);
        } catch (Throwable e) {
            for (IRPCExceptionHandler handler : handlers) {
                result = handler.handle(method, rpcRequest, e);
                if (result != null) {
                    return result;
                }
            }
            // default handler
            return RPC.encodeResponseForFailure(method, e, rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        }
        for (IRPCFinalizer finalizer : finalizers) {
            finalizer.cleanup();
        }
        return result;
    }

    protected void addProcessor(IAnnotationProcessor checker) {
        this.checkers.addLast(checker);
    }

    /**
     * Add the exception handler to handlers queue.
     * 
     * @param handler
     *            handler to add
     * @param important
     *            if true, the handler is put to the head of the queue; it's
     *            executed before the others and should also return null to not
     *            block further handlers
     */
    protected void addExceptionHandler(IRPCExceptionHandler handler, boolean important) {
        if (important) {
            handlers.addFirst(handler);
        } else {
            handlers.addLast(handler);
        }
    }

    protected void addFinalizer(IRPCFinalizer finalizer) {
        this.finalizers.addLast(finalizer);
    }
}
