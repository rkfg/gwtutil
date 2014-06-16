package ru.ppsrk.gwt.server;

import static ru.ppsrk.gwt.server.ServerUtils.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.HasId;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.ServerUtils.MapperHint;

public class HibernateUtil {

    public static class ListQueryFilter {
        private List<String> filterNames = new ArrayList<String>();
        private List<HashMap<String, Object>> filterParams = new ArrayList<HashMap<String, Object>>();

        public ListQueryFilter addFilter(String name, String[] paramNames, Object[] paramValues) throws LogicException {
            filterNames.add(name);
            HashMap<String, Object> tmpParams = new HashMap<String, Object>();
            if (paramNames != null && paramValues != null) {
                if (paramNames.length != paramValues.length) {
                    throw new LogicException("paramNames.length != paramValues.length in ListQueryFilter");
                }
                for (int i = 0; i < paramNames.length; i++) {
                    tmpParams.put(paramNames[i], paramValues[i]);
                }
            }
            filterParams.add(tmpParams);
            return this;
        }

        public void applyFilter(Session session) {
            for (int i = 0; i < filterNames.size(); i++) {
                Filter filter = session.enableFilter(filterNames.get(i));
                HashMap<String, Object> params = filterParams.get(i);
                for (Entry<String, Object> filterParam : params.entrySet()) {
                    filter.setParameter(filterParam.getKey(), filterParam.getValue());
                }
            }
        }
    }

    private static List<SessionFactory> sessionFactory = new ArrayList<SessionFactory>();

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

    public static <T> void deleteObject(final Class<T> objectClass, final Long id) throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) {
                session.delete(session.get(objectClass, id));
                return null;
            }
        });
    }

    public static <T> T exec(HibernateCallback<T> callback) throws LogicException, ClientAuthException {
        return exec(0, callback);
    }

    public static <T> T exec(int sessionNumber, HibernateCallback<T> callback) throws LogicException, ClientAuthException {
        T result = null;
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory(sessionNumber);
        if (sessionFactory != null) {
            Session session = sessionFactory.openSession();
            try {
                session.beginTransaction();
                result = callback.run(session);
                if (session.getTransaction().isActive()) {
                    session.getTransaction().commit();
                }
            } catch (HibernateException e) {
                session.getTransaction().rollback();
                throw e;
            } catch (LogicException e) {
                session.getTransaction().rollback();
                throw e;
            } catch (ClientAuthException e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                session.close();
            }
        }
        return result;
    }

    public static <T> T exec(int[] sessionNumbers, HibernateMultiSessionCallback<T> callback) throws LogicException, ClientAuthException {
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
                    if (!sessions[number].getTransaction().wasCommitted()) {
                        sessions[number].getTransaction().commit();
                    }
                }
            }
        } catch (HibernateException e) {
            for (int number = 0; number < sessionNumbers.length; number++) {
                if (sessions[number] != null) {
                    sessions[number].getTransaction().rollback();
                }
            }
            throw e;
        } catch (LogicException e) {
            for (int number = 0; number < sessionNumbers.length; number++) {
                if (sessions[number] != null) {
                    sessions[number].getTransaction().rollback();
                }
            }
            throw e;
        } catch (ClientAuthException e) {
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

    public static List<SessionFactory> getSessionFactories() {
        return sessionFactory;
    }

    public static SessionFactory getSessionFactory() {
        return getSessionFactory(0);
    }

    public static SessionFactory getSessionFactory(int nIndex) {
        if (nIndex >= 0 && nIndex < sessionFactory.size()) {
            return sessionFactory.get(nIndex);
        }
        return null;
    }

    public static void initSessionFactory(String cfgFilename) {
        ServiceRegistry serviceRegistry;
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Configuration configuration = new Configuration();
            configuration.configure(cfgFilename);
            serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
            sessionFactory.add(configuration.buildSessionFactory(serviceRegistry));
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static boolean initSessionFactoryDebugRelease(boolean forceDebug, boolean forceRelease, String debugCfg, String releaseCfg) {
        String debugStr = System.getenv("debug");
        boolean debug = false;
        if (!forceDebug && (forceRelease || debugStr == null || !debugStr.equals("yes"))) {
            HibernateUtil.initSessionFactory(releaseCfg);
        } else {
            HibernateUtil.initSessionFactory(debugCfg);
            debug = true;
        }
        return debug;
    }

    public static <DTO, H extends MapperHint> List<DTO> queryList(final String query, final String[] paramNames,
            final Object[] paramValues, final Class<DTO> clazz, final Class<H> hintClass) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<List<DTO>>() {

            @Override
            public List<DTO> run(Session session) throws LogicException, ClientAuthenticationException {
                return mapArray(queryList(query, paramNames, paramValues, session, (ListQueryFilter) null), clazz, hintClass);
            }
        });
    }

    public static <DTO extends HasId> List<DTO> queryList(final String query, String[] paramNames, Object[] paramValues,
            final Class<DTO> clazz) throws LogicException, ClientAuthException {
        return queryList(query, paramNames, paramValues, clazz, (ListQueryFilter) null);
    }

    public static <DTO extends HasId> List<DTO> queryList(final String query, String[] paramNames, Object[] paramValues, Session session,
            final Class<DTO> clazz) throws LogicException, ClientAuthException {
        return mapArray(queryList(query, paramNames, paramValues, session, (ListQueryFilter) null), clazz);
    }

    public static <DTO extends HasId> List<DTO> queryList(final String query, final String[] paramNames, final Object[] paramValues,
            final Class<DTO> clazz, final ListQueryFilter filter) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<List<DTO>>() {

            @Override
            public List<DTO> run(Session session) throws LogicException, ClientAuthenticationException {
                return mapArray(queryList(query, paramNames, paramValues, session, filter), clazz);
            }
        });
    }

    public static <HIB> List<HIB> queryList(final String query, final String[] paramNames, final Object[] paramValues,
            final ListQueryFilter filter) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<List<HIB>>() {

            @Override
            public List<HIB> run(Session session) throws LogicException, ClientAuthenticationException {
                return queryList(query, paramNames, paramValues, session, filter);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <HIB> List<HIB> queryList(final String query, String[] paramNames, Object[] paramValues, Session session,
            final ListQueryFilter filter) throws LogicException {
        if (paramNames.length != paramValues.length) {
            throw new LogicException("paramNames.length != paramValues.length");
        }
        final HashMap<String, Object> params = new HashMap<String, Object>();
        for (int i = 0; i < paramNames.length; i++) {
            params.put(paramNames[i], paramValues[i]);
        }
        if (filter != null) {
            filter.applyFilter(session);
        }
        return (List<HIB>) session.createQuery(query).setProperties(params).list();
    }

    public static void restartTransaction(Session session) {
        session.getTransaction().commit();
        session.beginTransaction();
    }

    public static <DTO extends HasId, HIB> DTO saveObject(final HIB hib, final Class<DTO> targetClass) throws LogicException,
            ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<DTO>() {

            @Override
            public DTO run(Session session) throws LogicException, ClientAuthException {
                return mapModel(session.merge(hib), targetClass);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <DTO extends HasId, HIB> DTO saveDTO(final DTO dto, final Class<HIB> targetClass) throws LogicException,
            ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<DTO>() {

            @Override
            public DTO run(Session session) throws LogicException, ClientAuthenticationException {
                return (DTO) mapModel(HibernateUtil.saveObject(dto, targetClass, true, session), dto.getClass());
            }
        });
    }

    public static <DTO extends HasId, HIB> HIB saveObject(final DTO objectDTO, final Class<HIB> classHIB) throws LogicException,
            ClientAuthException {
        return saveObject(objectDTO, classHIB, false);
    }

    public static <DTO extends HasId, HIB> HIB saveObject(final DTO objectDTO, final Class<HIB> classHIB, final boolean setId)
            throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<HIB>() {

            @Override
            public HIB run(Session session) throws LogicException {
                return saveObject(objectDTO, classHIB, setId, session);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <DTO extends HasId, HIB> HIB saveObject(final DTO objectDTO, final Class<HIB> classHIB, final boolean setId,
            Session session) {
        if (objectDTO.getId() != null) {
            return (HIB) session.merge(mapModel(objectDTO, classHIB));
        } else {
            Long id = (Long) session.save(mapModel(objectDTO, classHIB));
            HIB result = (HIB) session.get(classHIB, id);
            if (setId) {
                objectDTO.setId(id);
            }
            return result;
        }
    }

    public static <T> T saveObject(final T object) throws LogicException, ClientAuthException {
        return HibernateUtil.exec(new HibernateCallback<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T run(Session session) throws LogicException, ClientAuthenticationException {
                return (T) session.merge(object);
            }
        });
    }

    public static <T> T tryGetObject(Long id, Class<T> clazz, Session session, String failText) throws LogicException {
        @SuppressWarnings("unchecked")
        T result = (T) session.get(clazz, id);
        if (result == null) {
            throw new LogicException(failText);
        }
        return result;
    }

    public static <T> T tryGetObject(final Long id, final Class<T> clazz, final String failText) throws LogicException, ClientAuthException {
        return exec(new HibernateCallback<T>() {

            @Override
            public T run(Session session) throws LogicException, ClientAuthenticationException {
                return tryGetObject(id, clazz, session, failText);
            }
        });
    }

    public static <T, DTO extends HasId> DTO tryGetObject(final Long id, final Class<T> clazz, final String failText,
            final Class<DTO> dtoClass) throws LogicException, ClientAuthException {
        return exec(new HibernateCallback<DTO>() {

            @Override
            public DTO run(Session session) throws LogicException, ClientAuthenticationException {
                return tryGetObject(id, clazz, session, failText, dtoClass);
            }
        });
    }

    public static <T, DTO extends HasId> DTO tryGetObject(final Long id, final Class<T> clazz, Session session, final String failText,
            final Class<DTO> dtoClass) throws LogicException {
        return mapModel(tryGetObject(id, clazz, session, failText), dtoClass);
    }
}
