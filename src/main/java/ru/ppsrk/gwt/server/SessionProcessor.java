package ru.ppsrk.gwt.server;

import java.lang.reflect.Method;
import java.util.LinkedList;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.google.gwt.user.server.rpc.RPCRequest;

import ru.ppsrk.gwt.server.AnnotatedServlet.IAnnotationProcessor;
import ru.ppsrk.gwt.server.AnnotatedServlet.IRPCFinalizer;

public class SessionProcessor implements IAnnotationProcessor, IRPCFinalizer {

    private ThreadLocal<Session> sessionTL = new ThreadLocal<>();
    private ThreadLocal<LinkedList<Runnable>> oneTimers = ThreadLocal.withInitial(LinkedList::new);

    @Override
    public void process(Method implMethod, RPCRequest rpcRequest) throws Exception {
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

    public void setSession(Session session) {
        sessionTL.set(session);
    }

    public Session getSession() {
        return sessionTL.get();
    }

    @Override
    public void cleanup(boolean failure) {
        Session session = getSession();
        if (session != null) {
            try {
                if (failure) {
                    session.getTransaction().rollback();
                } else {
                    if (session.getTransaction().isActive()) {
                        session.getTransaction().commit();
                    }
                    LinkedList<Runnable> t = oneTimers.get();
                    t.stream().forEach(Runnable::run);
                    t.clear();
                }
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
                setSession(null);
            }
        }
    }

    public void registerTo(AnnotatedServlet annotatedServlet) {
        annotatedServlet.addProcessor(this);
        annotatedServlet.addFinalizer(this);
    }

    public void registerOneTimer(Runnable r) {
        oneTimers.get().add(r);
    }

}
