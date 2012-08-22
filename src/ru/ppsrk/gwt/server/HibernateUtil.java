package ru.ppsrk.gwt.server;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import ru.ppsrk.gwt.client.LogicException;

public class HibernateUtil {

    private static List<SessionFactory> sessionFactory = new ArrayList<SessionFactory>();
    private static ServiceRegistry serviceRegistry;

    public static void initSessionFactory(String cfgFilename) {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Configuration configuration = new Configuration();
            configuration.configure(cfgFilename);
            serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
            sessionFactory.add(configuration.buildSessionFactory(serviceRegistry));
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static List<SessionFactory> getSessionFactories() {
        return sessionFactory;
    }
    
    public static SessionFactory getSessionFactory(int nIndex) {
        return sessionFactory.get(nIndex);
    }

    public static SessionFactory getSessionFactory() {
        return getSessionFactory(0);
    }

    public static <T> T exec(HibernateCallback<T> callback) throws LogicException {
        Session session = HibernateUtil.getSessionFactory(0).openSession();
        T result = null;
        try {
            session.beginTransaction();
            result = callback.run(session);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
        return result;
    }
}