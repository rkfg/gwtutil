package ru.ppsrk.gwt.server;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.gwt.user.server.rpc.RPCRequest;

import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.server.AnnotatedServlet.IAnnotationProcessor;
import ru.ppsrk.gwt.shared.RequiresAnyRole;
import ru.ppsrk.gwt.shared.RequiresAuth;
import ru.ppsrk.gwt.shared.RequiresRoles;

public class AuthProcessor implements IAnnotationProcessor {

    @Override
    public void process(Method implMethod, RPCRequest rpcRequest) throws Throwable {
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
    }

}