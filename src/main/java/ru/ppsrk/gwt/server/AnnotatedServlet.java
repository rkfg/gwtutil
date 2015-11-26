package ru.ppsrk.gwt.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

    private List<IAnnotationProcessor> checkers = new ArrayList<IAnnotationProcessor>(5);
    private List<IRPCExceptionHandler> handlers = new ArrayList<IRPCExceptionHandler>(5);

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
        addExceptionHandler(new AlertHandler());
    }

    @Override
    public String processCall(String payload) throws SerializationException {
        RPCRequest rpcRequest = RPC.decodeRequest(payload, getClass(), this);
        Method method = rpcRequest.getMethod();
        try {
            Method implMethod = this.getClass().getMethod(method.getName(), method.getParameterTypes());
            AuthServiceImpl.setMDCIP(getThreadLocalRequest());
            for (IAnnotationProcessor checker : checkers) {
                checker.process(implMethod);
            }
        } catch (Throwable e) {
            for (IRPCExceptionHandler handler : handlers) {
                String result = handler.handle(method, rpcRequest, e);
                if (result != null) {
                    return result;
                }
            }
            // default handler
            return RPC.encodeResponseForFailure(method, e, rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        }
        return super.processCall(payload);
    }

    protected void addProcessor(IAnnotationProcessor checker) {
        this.checkers.add(checker);
    }

    protected void addExceptionHandler(IRPCExceptionHandler handler) {
        handlers.add(handler);
    }

}
