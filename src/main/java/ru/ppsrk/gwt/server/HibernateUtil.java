package ru.ppsrk.gwt.server;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.Hierarchic;
import ru.ppsrk.gwt.client.LogicException;

public class HibernateUtil {

    private static List<SessionFactory> sessionFactory = new ArrayList<SessionFactory>();

    public static void initSessionFactory(String cfgFilename) {
        ServiceRegistry serviceRegistry;
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

    public static void cleanup() {
        System.out.println("Cleaning up hibernate utils...");
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                System.out.println(String.format("Deregistering jdbc driver: %s", driver));
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                System.out.println(String.format("Error deregistering driver %s", driver));
            }
        }

        for (SessionFactory factory : sessionFactory) {
            System.out.println("Closing factory " + factory);
            factory.close();
        }
        sessionFactory = null;
    }

    public static List<SessionFactory> getSessionFactories() {
        return sessionFactory;
    }

    public static SessionFactory getSessionFactory(int nIndex) {
        if (nIndex >= 0 && nIndex < sessionFactory.size()) {
            return sessionFactory.get(nIndex);
        }
        return null;
    }

    public static SessionFactory getSessionFactory() {
        return getSessionFactory(0);
    }

    public static <T> T exec(HibernateCallback<T> callback) throws LogicException, ClientAuthenticationException {
        return exec(0, callback);
    }

    public static <T> T exec(int sessionNumber, HibernateCallback<T> callback) throws LogicException, ClientAuthenticationException {
        T result = null;
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory(sessionNumber);
        if (sessionFactory != null) {
            Session session = sessionFactory.openSession();
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
        }
        return result;
    }

    public static <T> T exec(int[] sessionNumbers, HibernateMultiSessionCallback<T> callback) throws LogicException, ClientAuthenticationException {
        Session[] sessions = new Session[sessionNumbers.length];
        T result = null;
        try {
            for (int number = 0; number < sessionNumbers.length; number++) {
                SessionFactory sessionFactory = HibernateUtil.getSessionFactory(sessionNumbers[number]);
                if (sessionFactory != null) {
                    sessions[number] = sessionFactory.openSession();
                    sessions[number].beginTransaction();
                }
            }
            result = callback.run(sessions);
            for (int number = 0; number < sessionNumbers.length; number++) {
                if (sessions[number] != null) {
                    sessions[number].getTransaction().commit();
                }
            }
        } catch (HibernateException e) {
            for (int number = 0; number < sessionNumbers.length; number++) {
                if (sessions[number] != null) {
                    sessions[number].getTransaction().rollback();
                }
            }
            throw e;
        } finally {
            for (int number = 0; number < sessionNumbers.length; number++) {
                if (sessions[number] != null) {
                    sessions[number].close();
                }
            }
        }
        return result;
    }

    public static <DTO extends Hierarchic, HIB> HIB saveObject(final DTO objectDTO, final Class<HIB> classHIB) throws LogicException,
            ClientAuthenticationException {
        return saveObject(objectDTO, classHIB, false);
    }

    public static <DTO extends Hierarchic, HIB> HIB saveObject(final DTO objectDTO, final Class<HIB> classHIB, final boolean setId) throws LogicException,
            ClientAuthenticationException {
        return HibernateUtil.exec(new HibernateCallback<HIB>() {

            @Override
            public HIB run(Session session) throws LogicException {
                return saveObject(objectDTO, classHIB, setId, session);
            }
        });
    }

    public static <T> void deleteObject(final Class<T> objectClass, final Long id) throws ClientAuthenticationException, LogicException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) {
                session.delete(session.get(objectClass, id));
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <DTO extends Hierarchic, HIB> HIB saveObject(final DTO objectDTO, final Class<HIB> classHIB, final boolean setId, Session session) {
        if (objectDTO.getId() != null) {
            return (HIB) session.merge(ServerUtils.mapModel(objectDTO, classHIB));
        } else {
            Long id = (Long) session.save(ServerUtils.mapModel(objectDTO, classHIB));
            HIB result = (HIB) session.get(classHIB, id);
            if (setId) {
                objectDTO.setId(id);
            }
            return result;
        }
    }

    public static void restartTransaction(Session session) {
        session.getTransaction().commit();
        session.beginTransaction();
    }

}