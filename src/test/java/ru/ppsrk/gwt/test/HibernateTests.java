package ru.ppsrk.gwt.test;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;

import ru.ppsrk.gwt.server.HibernateUtil;

public class HibernateTests {
    protected Session session;

    @Before
    public void setSession() {
        session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
    }

    @After
    public void cleanup() {
        session.getTransaction().rollback();
        session.close();
    }

}
