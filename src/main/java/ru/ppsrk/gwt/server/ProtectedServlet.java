package ru.ppsrk.gwt.server;

import java.lang.reflect.Method;
import java.util.Arrays;

import ru.ppsrk.gwt.client.AlertRuntimeException;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.shared.RequiresAnyRole;
import ru.ppsrk.gwt.shared.RequiresAuth;
import ru.ppsrk.gwt.shared.RequiresRoles;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ProtectedServlet extends RemoteServiceServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6677960915796966071L;

    @Override
    public String processCall(String payload) throws SerializationException {
        RPCRequest rpcRequest = RPC.decodeRequest(payload, getClass(), this);
        Method method = rpcRequest.getMethod();
        try {
            Method implMethod = this.getClass().getMethod(method.getName(), method.getParameterTypes());
            AuthServiceImpl.setMDCIP(getThreadLocalRequest());
            if (implMethod.isAnnotationPresent(RequiresAuth.class)) {
                AuthServiceImpl.requiresAuth();
            }
            if (implMethod.isAnnotationPresent(RequiresRoles.class)) {
                RequiresRoles rolesAnn = implMethod.getAnnotation(RequiresRoles.class);
                String[] rolesStr = rolesAnn.value();
                for (String role : rolesStr) {
                    AuthServiceImpl.requiresRole(role);
                }
            }
            if (implMethod.isAnnotationPresent(RequiresAnyRole.class)) {
                RequiresAnyRole rolesAnn = implMethod.getAnnotation(RequiresAnyRole.class);
                String[] rolesStr = rolesAnn.value();
                boolean allowed = false;
                for (String role : rolesStr) {
                    if (AuthServiceImpl.hasRole(role)) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) {
                    throw new ClientAuthorizationException("Not authorized (need any role: " + Arrays.toString(rolesStr) + ")");
                }
            }
        } catch (LogicException | ClientAuthException e) {
            return RPC.encodeResponseForFailure(method, e, rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        } catch (NoSuchMethodException e) {
            return RPC.encodeResponseForFailure(method, new AlertRuntimeException("method not found: " + method),
                    rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        } catch (SecurityException e) {
            return RPC.encodeResponseForFailure(method, new AlertRuntimeException("security exception on method resolving: " + method),
                    rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        }
        return super.processCall(payload);
    }
}
