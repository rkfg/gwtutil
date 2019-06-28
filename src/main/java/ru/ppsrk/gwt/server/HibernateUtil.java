package ru.ppsrk.gwt.server;

import static ru.ppsrk.gwt.server.ServerUtils.*;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.HasId;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.server.ServerUtils.MapperHint;

public class HibernateUtil {

    private HibernateUtil() {
    }

    public static class ListQueryFilter {
        private List<String> filterNames = new ArrayList<>();
        private List<Map<String, Object>> filterParams = new ArrayList<>();

        public ListQueryFilter addFilter(String name, String[] paramNames, Object[] paramValues) throws LogicException {
            filterNames.add(name);
            Map<String, Object> tmpParams = new HashMap<>();
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
                Map<String, Object> params = filterParams.get(i);
                for (Entry<String, Object> filterParam : params.entrySet()) {
                    filter.setParameter(filterParam.getKey(), filterParam.getValue());
                }
            }
        }
    }

    private static List<SessionFactory> sessionFactory = new ArrayList<>();

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

    public static void mysqlCleanup() {
        try {
            System.out.println("Shutting down abandoned cleanup thread...");
            AbandonedConnectionCleanupThread.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <T> void deleteObject(final Class<T> objectClass, final Long id) throws GwtUtilException {
        HibernateUtil.exec(session -> {
            session.delete(session.get(objectClass, id));
            return null;
        });
    }

    public static <T> T exec(HibernateCallback<T> callback) throws GwtUtilException {
        return exec(0, callback);
    }

    public static <T> T exec(int sessionNumber, HibernateCallback<T> callback) throws GwtUtilException {
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
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            } finally {
                session.close();
            }
        }
        return result;
    }

    public static <T> T exec(int[] sessionNumbers, HibernateMultiSessionCallback<T> callback) throws GwtUtilException {
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
                if (sessions[number] != null && sessions[number].getTransaction().isActive()) {
                    sessions[number].getTransaction().commit();
                }
            }
        } catch (Exception e) {
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
        } catch (Exception ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static boolean initSessionFactoryDebugRelease(boolean forceDebug, boolean forceRelease, String debugCfg, String releaseCfg) {
        return initSessionFactoryDebugRelease(forceDebug, forceRelease, debugCfg, releaseCfg, null);
    }

    public static boolean initSessionFactoryDebugRelease(boolean forceDebug, boolean forceRelease, String debugCfg, String releaseCfg,
            String releaseCfgOnDebug) {
        boolean debug = false;
        boolean debugMode = ServerUtils.isDebugMode();
        if (releaseCfgOnDebug != null && debugMode && forceRelease) {
            HibernateUtil.initSessionFactory(releaseCfgOnDebug);
            return false;
        }
        if (!forceDebug && (forceRelease || !debugMode)) {
            HibernateUtil.initSessionFactory(releaseCfg);
        } else {
            HibernateUtil.initSessionFactory(debugCfg);
            debug = true;
        }
        return debug;
    }

    public static <D, H extends MapperHint> List<D> queryList(final String query, final String[] paramNames, final Object[] paramValues,
            final Class<D> clazz, final Class<H> hintClass) throws GwtUtilException {
        return HibernateUtil.exec(session -> mapArray(queryList(query, paramNames, paramValues, session, (ListQueryFilter) null), clazz, hintClass));
    }

    public static <D extends HasId> List<D> queryList(final String query, String[] paramNames, Object[] paramValues,
            final Class<D> clazz) throws GwtUtilException {
        return queryList(query, paramNames, paramValues, clazz, (ListQueryFilter) null);
    }

    public static <D extends HasId> List<D> queryList(final String query, String[] paramNames, Object[] paramValues, Session session,
            final Class<D> clazz) throws GwtUtilException {
        return mapArray(queryList(query, paramNames, paramValues, session, (ListQueryFilter) null), clazz);
    }

    public static <D extends HasId> List<D> queryList(final String query, final String[] paramNames, final Object[] paramValues,
            final Class<D> clazz, final ListQueryFilter filter) throws GwtUtilException {
        return HibernateUtil.exec(session -> mapArray(queryList(query, paramNames, paramValues, session, filter), clazz));
    }

    public static <H> List<H> queryList(final String query, final String[] paramNames, final Object[] paramValues,
            final ListQueryFilter filter) throws GwtUtilException {
        return HibernateUtil.exec(session -> queryList(query, paramNames, paramValues, session, filter));
    }

    @SuppressWarnings("unchecked")
    public static <H> List<H> queryList(final String query, String[] paramNames, Object[] paramValues, Session session,
            final ListQueryFilter filter) throws LogicException {
        final HashMap<String, Object> params = new HashMap<>();
        if (paramNames != null && paramValues != null) {
            if (paramNames.length != paramValues.length) {
                throw new LogicException("paramNames.length != paramValues.length");
            }
            for (int i = 0; i < paramNames.length; i++) {
                params.put(paramNames[i], paramValues[i]);
            }
        }
        if (filter != null) {
            filter.applyFilter(session);
        }
        return (List<H>) session.createQuery(query).setProperties(params).setCacheable(true).list();
    }

    public static void restartTransaction(Session session) {
        session.getTransaction().commit();
        session.beginTransaction();
    }

    public static <D extends HasId, H> D saveObject(final H hib, final Class<D> targetClass) throws GwtUtilException {
        return HibernateUtil.exec(session -> mapModel(session.merge(hib), targetClass));
    }

    @SuppressWarnings("unchecked")
    public static <D extends HasId, H> D saveDTO(final D dto, final Class<H> targetClass) throws GwtUtilException {
        return HibernateUtil.exec(session -> (D) mapModel(HibernateUtil.saveObject(dto, targetClass, true, session), dto.getClass()));
    }

    public static <D extends HasId, H> H saveObject(final D objectDTO, final Class<H> classHIB) throws GwtUtilException {
        return saveObject(objectDTO, classHIB, false);
    }

    public static <D extends HasId, H> H saveObject(final D objectDTO, final Class<H> classHIB, final boolean setId)
            throws GwtUtilException {
        return HibernateUtil.exec(session -> saveObject(objectDTO, classHIB, setId, session));
    }

    @SuppressWarnings("unchecked")
    public static <D extends HasId, H> H saveObject(final D objectDTO, final Class<H> classHIB, final boolean setId,
            Session session) {
        if (objectDTO.getId() != null) {
            return (H) session.merge(mapModel(objectDTO, classHIB));
        } else {
            Long id = (Long) session.save(mapModel(objectDTO, classHIB));
            H result = (H) session.get(classHIB, id);
            if (setId) {
                objectDTO.setId(id);
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T saveObject(final T object) throws GwtUtilException {
        return HibernateUtil.exec(session -> (T) session.merge(object));
    }

    public static <T> T tryGetObject(Long id, Class<T> clazz, Session session, String failText) throws LogicException {
        @SuppressWarnings("unchecked")
        T result = (T) session.get(clazz, id);
        if (result == null) {
            throw new LogicException(failText);
        }
        return result;
    }

    public static <T> T tryGetObject(final Long id, final Class<T> clazz, final String failText) throws GwtUtilException {
        return exec(session -> tryGetObject(id, clazz, session, failText));
    }

    public static <T, D extends HasId> D tryGetObject(final Long id, final Class<T> clazz, final String failText,
            final Class<D> dtoClass) throws GwtUtilException {
        return exec(session -> tryGetObject(id, clazz, session, failText, dtoClass));
    }

    public static <T, D extends HasId> D tryGetObject(final Long id, final Class<T> clazz, Session session, final String failText,
            final Class<D> dtoClass) throws LogicException {
        return mapModel(tryGetObject(id, clazz, session, failText), dtoClass);
    }

    /**
     * Creates a new entity or merges it to the existing, replacing only non-null fields. Useful for updating incompletely mapped entities.
     * 
     * @param session
     * @param dto
     *            DTO entity
     * @param clazz
     *            Hibernate entity class
     * @return
     * @throws LogicException
     */
    public static <H extends HasId, D extends HasId> H saveOrUpdateDTO(Session session, D dto, Class<H> clazz)
            throws LogicException {
        H converted = mapModel(dto, clazz);
        return saveOrUpdateHIB(session, converted);
    }

    @SuppressWarnings("unchecked")
    public static <H extends HasId> H saveOrUpdateHIB(Session session, H entity) throws LogicException {
        if (entity.getId() != null) {
            H existing = (H) session.get(entity.getClass(), entity.getId());
            ServerUtils.mergeBeans(entity, existing);
            entity = existing;
        }
        return (H) session.merge(entity);
    }

    public static <H extends HasId, D extends HasId> H saveOrUpdateDTO(final D dto, final Class<H> clazz)
            throws GwtUtilException {
        return HibernateUtil.exec(session -> saveOrUpdateDTO(session, dto, clazz));
    }

    public static <H extends HasId> H saveOrUpdateHIB(final H entity) throws GwtUtilException {
        return HibernateUtil.exec(session -> saveOrUpdateHIB(session, entity));
    }
}
