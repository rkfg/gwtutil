package ru.ppsrk.gwt.server;

public class ProtectedServlet extends AnnotatedServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6677960915796966071L;

    public ProtectedServlet() {
        addProcessor(new AuthProcessor());
    }
}
