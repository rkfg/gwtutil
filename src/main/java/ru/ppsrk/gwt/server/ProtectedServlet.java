package ru.ppsrk.gwt.server;

import javax.servlet.ServletException;

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

    public ProtectedServlet() {
        addProcessor(new AuthProcessor());
    }

    @Override
    public void init() throws ServletException {
        super.init();
        sproc.registerTo(this);
    }
}
