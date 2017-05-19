package ru.ppsrk.gwt.server;

import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ru.ppsrk.gwt.client.AlertRuntimeException;

@SuppressWarnings("serial")
public abstract class AnnotatedServlet extends RemoteServiceServlet {

    private final transient Logger log = LoggerFactory.getLogger(getClass());
    
    public interface IAnnotationProcessor {
        /**
         * Process annotations
         * 
         * @param implMethod
         *            method called
         * @return serialized GWT response or null if there's nothing to return
         * @throws Throwable
         */
        public void process(Method implMethod, RPCRequest rpcRequest) throws Exception;
    }

    public interface IRPCFinalizer {
        public void cleanup(boolean failure);
    }

    private final transient Deque<IAnnotationProcessor> checkers = new LinkedList<>();
    private final transient Deque<IRPCFinalizer> finalizers = new LinkedList<>();

    @Override
    public String processCall(String payload) throws SerializationException {
        RPCRequest rpcRequest = RPC.decodeRequest(payload, getClass(), this);
        Method method = rpcRequest.getMethod();
        String result = null;
        try {
            Method implMethod = this.getClass().getMethod(method.getName(), method.getParameterTypes());
            AuthServiceImpl.setMDCIP(getThreadLocalRequest());
            for (IAnnotationProcessor checker : checkers) {
                checker.process(implMethod, rpcRequest);
            }
        } catch (NoSuchMethodException e) {
            return RPC.encodeResponseForFailure(method, new AlertRuntimeException("method not found: " + method),
                    rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        } catch (SecurityException e) {
            log.warn("Security exception on method resolving: ", e);
            return RPC.encodeResponseForFailure(method, new AlertRuntimeException("security exception on method resolving: " + method),
                    rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());

        } catch (Exception e) {
            return RPC.encodeResponseForFailure(method, e, rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        }
        result = super.processCall(payload);
        boolean failure = result.startsWith("//EX");
        for (IRPCFinalizer finalizer : finalizers) {
            finalizer.cleanup(failure);
        }
        return result;
    }

    protected void addProcessor(IAnnotationProcessor checker) {
        this.checkers.addLast(checker);
    }

    protected void addFinalizer(IRPCFinalizer finalizer) {
        this.finalizers.addLast(finalizer);
    }
}
