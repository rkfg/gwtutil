package ru.ppsrk.gwt.server;

import javax.servlet.ServletException;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

public class ProtectedServlet extends AnnotatedServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6677960915796966071L;

    protected final SessionProcessor sproc = new SessionProcessor();

    protected Session getSession() {
        return sproc.getSession();
    }

    protected Query createQuery(String query) {
        return sproc.getSession().createQuery(query);
    }

    protected Criteria createCriteria(Class<?> clazz) {
        return sproc.getSession().createCriteria(clazz);
    }

    protected Query createCachedQuery(String query) {
        return createQuery(query).setCacheable(true);
    }

    protected Criteria createCachedCriteria(Class<?> clazz) {
        return createCriteria(clazz).setCacheable(true);
    }

    public ProtectedServlet() {
        addProcessor(new AuthProcessor());
    }

    @Override
    public void init() throws ServletException {
        super.init();
        sproc.registerTo(this);
    }
}
