package ru.ppsrk.gwt.server;

import java.lang.reflect.Method;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gwt.user.server.rpc.RPCRequest;

import ru.ppsrk.gwt.server.AnnotatedServlet.IAnnotationProcessor;
import ru.ppsrk.gwt.server.AnnotatedServlet.IRPCExceptionHandler;
import ru.ppsrk.gwt.server.AnnotatedServlet.IRPCFinalizer;

public class SessionProcessor implements IAnnotationProcessor, IRPCExceptionHandler, IRPCFinalizer {

    private ThreadLocal<Session> sessionTL = new ThreadLocal<>();
    
    @Override
    public void process(Method implMethod) throws Throwable {
        if (implMethod.isAnnotationPresent(RequiresSession.class)) {
            int sessionNumber = implMethod.getAnnotation(RequiresSession.class).sessionNumber();
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory(sessionNumber);
            if (sessionFactory != null) {
                Session session = sessionFactory.openSession();
                session.beginTransaction();
                setSession(session);
            }
        }
    }

    @Override
    public String handle(Method method, RPCRequest rpcRequest, Throwable e) {
        Session session = getSession();
        if (session != null) {
            session.getTransaction().rollback();
            session.close();
            setSession(null);
        }
        return null;
    }

    public void setSession(Session session) {
        sessionTL.set(session);
    }

    public Session getSession() {
        return sessionTL.get();
    }

    @Override
    public void cleanup() {
        Session session = getSession();
        if (session != null && session.getTransaction().isActive()) {
            session.getTransaction().commit();
        }
    }

    public void registerTo(AnnotatedServlet annotatedServlet) {
        annotatedServlet.addProcessor(this);
        annotatedServlet.addExceptionHandler(this, true);
        annotatedServlet.addFinalizer(this);
    }

}
